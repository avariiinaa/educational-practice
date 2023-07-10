package com.example.ahocorasick

import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.Window
import java.io.IOException


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
    fun changeScene(fxml: String) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource(fxml))
        //stage?.scene = Scene(fxmlLoader.load(), 800.0, 800.0)
        stage?.scene?.root = fxmlLoader.load()
        stage?.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}