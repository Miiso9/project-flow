package ba.sum.fsre.projectflow.document;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Document;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> documents = new ArrayList<>();
    private OnDocumentClickListener listener;

    public interface OnDocumentClickListener {
        void onDownloadClick(Document document);
        void onDeleteClick(Document document);
    }

    public DocumentAdapter(OnDocumentClickListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.bind(documents.get(position));
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView name, uploader;
        ImageButton btnDownload, btnDelete;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.docName);
            uploader = itemView.findViewById(R.id.docUploader);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnDelete = itemView.findViewById(R.id.btnDeleteDoc);
        }

        void bind(Document doc) {
            name.setText(doc.fileName);

            if (doc.uploadedAt != null && doc.uploadedAt.length() > 10) {
                // Simple date parsing or just showing name
                String date = doc.uploadedAt.substring(0, 10);
                uploader.setText(date);
            } else {
                uploader.setText("Unknown date");
            }


            btnDelete.setOnClickListener(v -> listener.onDeleteClick(doc));
            btnDownload.setOnClickListener(v -> listener.onDownloadClick(doc));
        }
    }
}