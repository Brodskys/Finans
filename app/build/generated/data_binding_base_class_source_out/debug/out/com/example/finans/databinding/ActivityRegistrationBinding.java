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

public final class ActivityRegistrationBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button authUserBtn;

  @NonNull
  public final ImageView confirmPasswordIcon;

  @NonNull
  public final LinearLayout confirmPasswordLayout;

  @NonNull
  public final TextView confirmPasswordTxt;

  @NonNull
  public final EditText createUserTextConfirmPassword;

  @NonNull
  public final EditText createUserTextEmailAddress;

  @NonNull
  public final EditText createUserTextPassword;

  @NonNull
  public final ImageView emailIcon;

  @NonNull
  public final LinearLayout emailLayout;

  @NonNull
  public final TextView emailTxt;

  @NonNull
  public final LinearLayout linearLayout;

  @NonNull
  public final ImageView passwordIcon;

  @NonNull
  public final LinearLayout passwordLayout;

  @NonNull
  public final TextView passwordTxt;

  @NonNull
  public final Button registrationBack;

  private ActivityRegistrationBinding(@NonNull ConstraintLayout rootView,
      @NonNull Button authUserBtn, @NonNull ImageView confirmPasswordIcon,
      @NonNull LinearLayout confirmPasswordLayout, @NonNull TextView confirmPasswordTxt,
      @NonNull EditText createUserTextConfirmPassword, @NonNull EditText createUserTextEmailAddress,
      @NonNull EditText createUserTextPassword, @NonNull ImageView emailIcon,
      @NonNull LinearLayout emailLayout, @NonNull TextView emailTxt,
      @NonNull LinearLayout linearLayout, @NonNull ImageView passwordIcon,
      @NonNull LinearLayout passwordLayout, @NonNull TextView passwordTxt,
      @NonNull Button registrationBack) {
    this.rootView = rootView;
    this.authUserBtn = authUserBtn;
    this.confirmPasswordIcon = confirmPasswordIcon;
    this.confirmPasswordLayout = confirmPasswordLayout;
    this.confirmPasswordTxt = confirmPasswordTxt;
    this.createUserTextConfirmPassword = createUserTextConfirmPassword;
    this.createUserTextEmailAddress = createUserTextEmailAddress;
    this.createUserTextPassword = createUserTextPassword;
    this.emailIcon = emailIcon;
    this.emailLayout = emailLayout;
    this.emailTxt = emailTxt;
    this.linearLayout = linearLayout;
    this.passwordIcon = passwordIcon;
    this.passwordLayout = passwordLayout;
    this.passwordTxt = passwordTxt;
    this.registrationBack = registrationBack;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityRegistrationBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityRegistrationBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_registration, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityRegistrationBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.authUser_btn;
      Button authUserBtn = ViewBindings.findChildViewById(rootView, id);
      if (authUserBtn == null) {
        break missingId;
      }

      id = R.id.confirm_passwordIcon;
      ImageView confirmPasswordIcon = ViewBindings.findChildViewById(rootView, id);
      if (confirmPasswordIcon == null) {
        break missingId;
      }

      id = R.id.confirm_password_layout;
      LinearLayout confirmPasswordLayout = ViewBindings.findChildViewById(rootView, id);
      if (confirmPasswordLayout == null) {
        break missingId;
      }

      id = R.id.confirm_password_txt;
      TextView confirmPasswordTxt = ViewBindings.findChildViewById(rootView, id);
      if (confirmPasswordTxt == null) {
        break missingId;
      }

      id = R.id.createUserTextConfirmPassword;
      EditText createUserTextConfirmPassword = ViewBindings.findChildViewById(rootView, id);
      if (createUserTextConfirmPassword == null) {
        break missingId;
      }

      id = R.id.createUserTextEmailAddress;
      EditText createUserTextEmailAddress = ViewBindings.findChildViewById(rootView, id);
      if (createUserTextEmailAddress == null) {
        break missingId;
      }

      id = R.id.createUserTextPassword;
      EditText createUserTextPassword = ViewBindings.findChildViewById(rootView, id);
      if (createUserTextPassword == null) {
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

      id = R.id.linearLayout;
      LinearLayout linearLayout = ViewBindings.findChildViewById(rootView, id);
      if (linearLayout == null) {
        break missingId;
      }

      id = R.id.passwordIcon;
      ImageView passwordIcon = ViewBindings.findChildViewById(rootView, id);
      if (passwordIcon == null) {
        break missingId;
      }

      id = R.id.password_layout;
      LinearLayout passwordLayout = ViewBindings.findChildViewById(rootView, id);
      if (passwordLayout == null) {
        break missingId;
      }

      id = R.id.password_txt;
      TextView passwordTxt = ViewBindings.findChildViewById(rootView, id);
      if (passwordTxt == null) {
        break missingId;
      }

      id = R.id.registration_back;
      Button registrationBack = ViewBindings.findChildViewById(rootView, id);
      if (registrationBack == null) {
        break missingId;
      }

      return new ActivityRegistrationBinding((ConstraintLayout) rootView, authUserBtn,
          confirmPasswordIcon, confirmPasswordLayout, confirmPasswordTxt,
          createUserTextConfirmPassword, createUserTextEmailAddress, createUserTextPassword,
          emailIcon, emailLayout, emailTxt, linearLayout, passwordIcon, passwordLayout, passwordTxt,
          registrationBack);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}