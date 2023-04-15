// Generated by view binder compiler. Do not edit!
package com.example.finans.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.finans.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentBottomSheetPasswordResetBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ConstraintLayout bottomSheet;

  @NonNull
  public final EditText editTextTextEmailAddressReset;

  @NonNull
  public final ImageView emailIcon;

  @NonNull
  public final LinearLayout emailLayout;

  @NonNull
  public final TextView emailTxt;

  @NonNull
  public final LinearLayout languageLayout;

  @NonNull
  public final Button resetPasswordBtn;

  @NonNull
  public final TextView resetPasswordExit;

  @NonNull
  public final TextView textResetPassword;

  private FragmentBottomSheetPasswordResetBinding(@NonNull ConstraintLayout rootView,
      @NonNull ConstraintLayout bottomSheet, @NonNull EditText editTextTextEmailAddressReset,
      @NonNull ImageView emailIcon, @NonNull LinearLayout emailLayout, @NonNull TextView emailTxt,
      @NonNull LinearLayout languageLayout, @NonNull Button resetPasswordBtn,
      @NonNull TextView resetPasswordExit, @NonNull TextView textResetPassword) {
    this.rootView = rootView;
    this.bottomSheet = bottomSheet;
    this.editTextTextEmailAddressReset = editTextTextEmailAddressReset;
    this.emailIcon = emailIcon;
    this.emailLayout = emailLayout;
    this.emailTxt = emailTxt;
    this.languageLayout = languageLayout;
    this.resetPasswordBtn = resetPasswordBtn;
    this.resetPasswordExit = resetPasswordExit;
    this.textResetPassword = textResetPassword;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentBottomSheetPasswordResetBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentBottomSheetPasswordResetBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_bottom_sheet_password_reset, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentBottomSheetPasswordResetBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      ConstraintLayout bottomSheet = (ConstraintLayout) rootView;

      id = R.id.editTextTextEmailAddressReset;
      EditText editTextTextEmailAddressReset = ViewBindings.findChildViewById(rootView, id);
      if (editTextTextEmailAddressReset == null) {
        break missingId;
      }

      id = R.id.emailIcon;
      ImageView emailIcon = ViewBindings.findChildViewById(rootView, id);
      if (emailIcon == null) {
        break missingId;
      }

      id = R.id.email_layout;
      LinearLayout emailLayout = ViewBindings.findChildViewById(rootView, id);
      if (emailLayout == null) {
        break missingId;
      }

      id = R.id.email_txt;
      TextView emailTxt = ViewBindings.findChildViewById(rootView, id);
      if (emailTxt == null) {
        break missingId;
      }

      id = R.id.languageLayout;
      LinearLayout languageLayout = ViewBindings.findChildViewById(rootView, id);
      if (languageLayout == null) {
        break missingId;
      }

      id = R.id.resetPassword_btn;
      Button resetPasswordBtn = ViewBindings.findChildViewById(rootView, id);
      if (resetPasswordBtn == null) {
        break missingId;
      }

      id = R.id.resetPasswordExit;
      TextView resetPasswordExit = ViewBindings.findChildViewById(rootView, id);
      if (resetPasswordExit == null) {
        break missingId;
      }

      id = R.id.textResetPassword;
      TextView textResetPassword = ViewBindings.findChildViewById(rootView, id);
      if (textResetPassword == null) {
        break missingId;
      }

      return new FragmentBottomSheetPasswordResetBinding((ConstraintLayout) rootView, bottomSheet,
          editTextTextEmailAddressReset, emailIcon, emailLayout, emailTxt, languageLayout,
          resetPasswordBtn, resetPasswordExit, textResetPassword);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}