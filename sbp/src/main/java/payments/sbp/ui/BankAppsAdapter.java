package payments.sbp.ui;

import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import payments.sbp.R;


public class BankAppsAdapter extends RecyclerView.Adapter<BankAppsAdapter.ViewHolder> {
    private List<ResolveInfo> apps;
    ClickListener clickListener;
    String link;

    public interface ClickListener {
        /**
         * handler of selecting bank app
         *
         * @param link
         * @param info
         */
        void onClick(Activity context, String link, ResolveInfo info);
    }

    public BankAppsAdapter(ClickListener clickListener, String link) {
        this.clickListener = clickListener;
        this.link = link;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(apps.get(position));
    }

    @Override
    public int getItemCount() {
        return apps == null ? 0 : apps.size();
    }

    public void setApps(List<ResolveInfo> apps) {
        this.apps = apps;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgLogo;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.imgLogo);
            tvName = itemView.findViewById(R.id.tvName);
        }

        public void bindData(ResolveInfo info) {
            itemView.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (clickListener != null)
                    clickListener.onClick((Activity) v.getContext(),link, info);
            });

            tvName.setText(info.activityInfo.loadLabel(tvName.getContext().getPackageManager()));
            imgLogo.setImageDrawable(info.activityInfo.loadIcon(tvName.getContext().getPackageManager()));
        }
    }
}

