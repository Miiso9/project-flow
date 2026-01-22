package ba.sum.fsre.projectflow.repository;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import ba.sum.fsre.projectflow.model.Document;
import ba.sum.fsre.projectflow.network.Constants;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentRepository {
    private SupabaseApi api;
    private Context context;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB

    public DocumentRepository(SupabaseApi api, Context context) {
        this.api = api;
        this.context = context;
    }

    public interface DocumentCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getDocuments(String taskId, DocumentCallback<List<Document>> callback) {
        String select = "*,uploader:users(*)";
        api.getDocumentsForTask("eq." + taskId, select, "uploaded_at.desc")
                .enqueue(new Callback<List<Document>>() {
                    @Override
                    public void onResponse(Call<List<Document>> call, Response<List<Document>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Failed to fetch documents");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Document>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void uploadDocument(Uri fileUri, String taskId, String projectId, String userId, DocumentCallback<Document> callback) {
        long fileSize = getFileSize(fileUri);
        if (fileSize > MAX_FILE_SIZE) {
            callback.onError("File is too large! Limit is 50MB.");
            return;
        }

        File file = prepareFile(fileUri);
        if (file == null) {
            callback.onError("Could not process file");
            return;
        }

        String fileName = file.getName();
        String uniqueName = System.currentTimeMillis() + "_" + fileName;
        String storagePath = taskId + "/" + uniqueName;

        RequestBody requestFile = RequestBody.create(MediaType.parse(getMimeType(fileUri)), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", uniqueName, requestFile);

        api.uploadFile(storagePath, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String fullUrl = Constants.BASE_URL + "/storage/v1/object/public/task_documents/" + storagePath;

                    Document doc = new Document(taskId, projectId, userId, fullUrl, fileName);
                    createDocumentRecord(doc, callback);
                } else if (response.code() == 413) {
                    callback.onError("Upload failed: File is too large for the server.");
                } else {
                    callback.onError("Upload failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Upload network error: " + t.getMessage());
            }
        });
    }

    private void createDocumentRecord(Document doc, DocumentCallback<Document> callback) {
        api.createDocument(doc).enqueue(new Callback<List<Document>>() {
            @Override
            public void onResponse(Call<List<Document>> call, Response<List<Document>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Failed to save document metadata");
                }
            }

            @Override
            public void onFailure(Call<List<Document>> call, Throwable t) {
                callback.onError("DB Error: " + t.getMessage());
            }
        });
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals("content")) {
            android.content.ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    private File prepareFile(Uri uri) {
        try {
            String fileName = getFileName(uri);
            File tempFile = new File(context.getCacheDir(), fileName);

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if(index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    public void deleteDocument(Document document, DocumentCallback<Void> callback) {

        String storagePath = null;
        if (document.fileUrl != null && document.fileUrl.contains("task_documents/")) {
            String[] parts = document.fileUrl.split("task_documents/");
            if (parts.length > 1) {
                storagePath = parts[1];
            }
        }

        if (storagePath == null) {
            deleteFromDb(document.id, callback);
            return;
        }

        api.deleteFileFromStorage(storagePath).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                deleteFromDb(document.id, callback);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                deleteFromDb(document.id, callback);
            }
        });
    }

    private void deleteFromDb(String documentId, DocumentCallback<Void> callback) {
        api.deleteDocument("eq." + documentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete document record");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                if (index != -1) {
                    size = cursor.getLong(index);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
}