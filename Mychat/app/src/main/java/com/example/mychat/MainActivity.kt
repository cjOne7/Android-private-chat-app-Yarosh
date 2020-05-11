package com.example.mychat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseListAdapter
import com.github.library.bubbleview.BubbleTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText

class MainActivity : AppCompatActivity() {
    private lateinit var activityMain: RelativeLayout
    private lateinit var listAdapter //позволяет адаптировать те данные, которые мы получаем из базы данных в объеуты, что есть внутри Android Studio
            : FirebaseListAdapter<Message>
    private lateinit var emojiconEditText: EmojiconEditText
    private lateinit var submitButton: FloatingActionButton
    private lateinit var emojiButton: ImageView
    private lateinit var emojIconActions: EmojIconActions

    companion object {
        private const val SIGN_IN_CODE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(activityMain, "You are authorized.", Snackbar.LENGTH_LONG).show()//if authorised
                displayAllMessages()
            } else {
                Snackbar.make(activityMain, "You are NOT authorized.", Snackbar.LENGTH_SHORT).show()//if not authorised
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityMain = findViewById(R.id.activity_main)

        submitButton = findViewById(R.id.btnSend)
        emojiButton = findViewById(R.id.emoji_btn)
        emojiconEditText = findViewById(R.id.messageField)
        emojiButton.setImageResource(R.mipmap.smiley)

        emojIconActions = EmojIconActions(applicationContext, activityMain, emojiconEditText, emojiButton)
        emojIconActions.ShowEmojIcon()

        // this line causes the selected image to be displayed when collapsing the keyboard with emoticons
        emojIconActions.setIconsIds(R.drawable.ic_action_keyboard, R.mipmap.smiley)

        submitButton.setOnClickListener(View.OnClickListener {
            if (emojiconEditText.text.toString().isEmpty()) return@OnClickListener
            FirebaseDatabase.getInstance().reference.push().setValue(
                    FirebaseAuth.getInstance().currentUser!!.displayName?.let { it1 ->
                        Message(
                                it1,
                                emojiconEditText.text.toString()
                        )
                    }
            )
            emojiconEditText.setText("")
        })
        //Control if user is still not authorized
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE)//allows you to authorize the user
        } else {
            Snackbar.make(activityMain, "You are authorized.", Snackbar.LENGTH_SHORT).show()//if user is, show message and user has been authorised
            displayAllMessages()
        }
    }

    private fun displayAllMessages() {
        val listView = findViewById<ListView>(R.id.listOfMessages)
        listAdapter = object : FirebaseListAdapter<Message>(
                this,
                Message::class.java,
                R.layout.list_item,
                FirebaseDatabase.getInstance().reference) {//connection to database
            override fun populateView(v: View, model: Message, position: Int) { //install messages in the adapter with the design spelled out in list_item.xml
                val msgUser: TextView = v.findViewById(R.id.messageUser) // specify that we find the objects we need inside the View v window
                val msgTime: TextView = v.findViewById(R.id.messageTime)//get message time
                val msgText: BubbleTextView = v.findViewById(R.id.messageText)//get message text
                msgUser.text = model.userName
                msgText.text = model.messageText
                msgTime.text = DateFormat.format("dd MMMM yyyy HH:mm:ss", model.messageTime)
            }
        }
        listView.adapter = listAdapter
    }
}