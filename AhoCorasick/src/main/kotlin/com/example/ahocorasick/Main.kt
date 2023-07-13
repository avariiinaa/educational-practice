package com.example.ahocorasick

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage


class HelloApplication : Application() {

    private var stage: Stage? = null //oho

    override fun start(stage1: Stage?) {
        this.stage = stage1
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 800.0, 800.0)
        stage1?.title = "aho-corasick"
        stage1?.scene = scene
        stage1?.show()

    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}