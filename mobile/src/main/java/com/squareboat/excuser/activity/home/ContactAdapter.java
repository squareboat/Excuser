package com.squareboat.excuser.activity.home;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareboat.excuser.R;
import com.squareboat.excuser.model.Contact;
import com.squareboat.excuser.widget.textdrawable.TextDrawable;
import com.squareboat.excuser.widget.textdrawable.util.ColorGenerator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface Callbacks {
        public void onContactClick(Contact contact);
        public void onContactDelete(Contact contact);
    }

    private Callbacks mCallbacks;
    private Context context;
    private List<Contact> mFeedList;

    public ContactAdapter(List<Contact> feedList) {
        this.mFeedList = feedList;
    }

    @Override
    public ContactViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, final int position) {
        final Contact contact = mFeedList.get(position);

        if(contact.getName().isEmpty()) {
            holder.contactName.setText("Untitled");
            holder.contactMobile.setText(contact.getMobile());
        } else {
            holder.contactName.setText(contact.getName());
            holder.contactMobile.setText(contact.getMobile());
        }

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        TextDrawable drawableInitial = TextDrawable.builder().buildRound(String.valueOf(holder.contactName.getText().toString().charAt(0)), generator.getRandomColor()); // radius in px

        holder.contactImage.setImageDrawable(drawableInitial);

        holder.contactOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, v);
                popup.inflate(R.menu.menu_contact_options);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_edit_contact:
                                {
                                    if(mCallbacks!=null)
                                        mCallbacks.onContactClick(contact);
                                } break;

                            case R.id.menu_delete_contact: {
                                if(mCallbacks!=null)
                                    mCallbacks.onContactDelete(contact);
                            } break;

                        }
                        return true;
                    }
                });

                popup.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return (mFeedList!=null? mFeedList.size():0);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_contact_name)
        TextView contactName;

        @BindView(R.id.text_contact_mobile)
        TextView contactMobile;

        @BindView(R.id.image_contact)
        AppCompatImageView contactImage;

        @BindView(R.id.button_contact_options)
        AppCompatImageView contactOptions;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void onItemAdded(Contact category){
        mFeedList.add(category);
        notifyDataSetChanged();
    }

}
