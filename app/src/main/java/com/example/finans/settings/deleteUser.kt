package com.example.finans.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.finans.authorization.AuthorizationActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

fun deleteUser (sharedPreferences: SharedPreferences, context: Activity){

    //Удаление из авторизации
    Firebase.auth.currentUser!!.delete()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) { }
        }


    //Удаление из хранилища


    var storageRef = FirebaseStorage.getInstance().reference.child("images/${Firebase.auth.uid.toString()}")

    storageRef.listAll().addOnSuccessListener { listResult ->
        var deleteTasks = listResult.items.map { it.delete() }
        Tasks.whenAllComplete(deleteTasks).addOnCompleteListener {
            storageRef.delete().addOnCompleteListener {
                storageRef = FirebaseStorage.getInstance().reference.child("images/${Firebase.auth.uid.toString()}/operation")

                storageRef.listAll().addOnSuccessListener { listResult ->
                    deleteTasks = listResult.items.map { it.delete() }
                    Tasks.whenAllComplete(deleteTasks).addOnCompleteListener {
                        storageRef.delete().addOnCompleteListener {
                        }
                    }
                }.addOnFailureListener {}
            }
        }
    }.addOnFailureListener {}



    //Удаление данных

    val uid = Firebase.firestore.collection("users").document(Firebase.auth.uid.toString())

    var ref = uid.collection("user")

    deleteCollection(ref)

    ref = uid.collection("category")

    deleteCollection(ref)

    ref = uid.collection("operation")

    deleteCollection(ref)


    val editor = sharedPreferences.edit()
    editor?.remove("Pincode")
    editor?.apply()

    val editor2 = sharedPreferences.edit()
    editor2?.remove("currency")
    editor2?.apply()

    val editor3 = sharedPreferences.edit()
    editor3?.remove("modeSwitch")
    editor3?.apply()

    Firebase.auth.signOut()
    context.startActivity(Intent(context, AuthorizationActivity::class.java))
    context.finish()

}


fun deleteCollection(collection: CollectionReference) {
    collection.get()
        .addOnSuccessListener { snapshot ->
            for (document in snapshot.documents) {
                val documentRef = collection.document(document.id)
                deleteCollection(documentRef.collection("subcategories"))
                documentRef.delete()
            }
        }
    collection.document().delete()
}