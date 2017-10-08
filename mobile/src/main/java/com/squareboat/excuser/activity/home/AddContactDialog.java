package com.squareboat.excuser.activity.home;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareboat.excuser.R;
import com.squareboat.excuser.model.Contact;

/**
 * Created by Vipul on 03/01/17.
 */

public class AddContactDialog extends DialogFragment {

    private DialogCallback mDialogCallback;
    private View positiveAction, negativeAction;
    private TextInputLayout nameInputLayout, mobileInputLayout;
    private TextInputEditText nameEditText, mobileEditText;
    private Contact mContact;
    private MaterialDialog materialDialog;

    public static void showDialog(FragmentManager fragmentManager, Contact contact, DialogCallback dialogCallback) {
        AddContactDialog dialog = new AddContactDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("contact", contact);
        dialog.setArguments(bundle);
        dialog.setDialogCallback(dialogCallback);
        dialog.show(fragmentManager, "[ADD_CONTACT_DIALOG]");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.mContact = getArguments().getParcelable("contact");

        materialDialog = new MaterialDialog.Builder(getActivity())
                .title(mContact == null ? "Add Contact" : "Edit Contact")
                .customView(R.layout.dialog_add_contact, true)
                .cancelable(false)
                .positiveText(mContact == null ? R.string.add : R.string.update)
                .negativeText(android.R.string.cancel)
                .build();

        positiveAction = materialDialog.getActionButton(DialogAction.POSITIVE);
        negativeAction = materialDialog.getActionButton(DialogAction.NEGATIVE);
        nameEditText = (TextInputEditText) materialDialog.getCustomView().findViewById(R.id.edit_name);
        mobileEditText = (TextInputEditText) materialDialog.getCustomView().findViewById(R.id.edit_mobile);
        nameInputLayout = (TextInputLayout) materialDialog.getCustomView().findViewById(R.id.input_layout_name);
        mobileInputLayout = (TextInputLayout) materialDialog.getCustomView().findViewById(R.id.input_layout_mobile);

        if (mContact != null) {
            nameEditText.setText(mContact.getName());
            mobileEditText.setText(mContact.getMobile());
        }

        positiveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nameInputLayout.setErrorEnabled(false);
                nameInputLayout.setError(null);

                mobileInputLayout.setErrorEnabled(false);
                mobileInputLayout.setError(null);

                String name = nameEditText.getText().toString().trim();
                String mobile = mobileEditText.getText().toString().trim();

                if (mobile.isEmpty()) {
                    mobileInputLayout.setError("Please enter mobile");
                    return;
                }

                if (mDialogCallback != null) {

                    if (mContact == null) {
                        mContact = new Contact();
                        mContact.setName(name);
                        mContact.setMobile(mobile);
                        mDialogCallback.onContactAdded(mContact);
                    } else {
                        mContact.setName(name);
                        mContact.setMobile(mobile);
                        mDialogCallback.onContactUpdated(mContact);
                    }

                    dismiss();
                }

            }
        });

        return materialDialog;
    }

    public void setDialogCallback(DialogCallback dialogCallback) {
        this.mDialogCallback = dialogCallback;
    }

    public interface DialogCallback {
        void onContactAdded(Contact contact);

        void onContactUpdated(Contact contact);
    }

}
