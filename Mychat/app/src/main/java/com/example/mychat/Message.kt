package com.example.mychat

import java.util.*

class Message {
    var userName: String
    var messageText: String
    var messageTime: Long = 0

    constructor() {
        userName = ""
        messageText = ""
    }

    constructor(userName: String, messageText: String) {
        this.userName = userName
        this.messageText = messageText
        this.messageTime = Date().time
    }

}