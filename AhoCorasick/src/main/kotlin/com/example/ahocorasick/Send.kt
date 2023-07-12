package com.example.ahocorasick


import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView.EditEvent
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.Text
import java.util.regex.Pattern


class Bor {
    /**
     * Класс вершины бора
     */
    class Node(
        var number: Int? = null,
        val father: Node? = null,
        protected val pchar: Char? = null,
        var indexes: IntArray? = null
    ) {
        var sufflink: Node? = null
        var terminal = false
        var go: MutableMap<Char, Node> = mutableMapOf()
        private var children: MutableList<Pair<Node, Char>>? = null
        var subPatterns = mutableSetOf<Node>()
        var flagSubPatterns = false

        /**
         * Возвращает true если существует дочерний Node с
        символом-аргументов ведущим к нему
         */
        operator fun contains(sym: Char): Boolean {
            if (children == null) return false
            return children!!.any {
                it.second == sym
            }
        }

        /**
         * Возвращает объект Node из массива детей, если к нему
        ведет символ поиска (аргумент)
         * Возвращает null при отсутствии детей с запрашиваемым
        символом
         */
        fun getNode(sym: Char): Node? {
            if (children == null) { // нет потомков
                return null
            } else {// найти потомка к которому можно прийти по символу
                return children!!.firstOrNull {
                    it.second == sym
                }?.first
            }
        }

        /**
         * Возвращает объект Node в который переходят по
        символу-аргументу
         */

        fun addChildren(sym: Char): Node {
            // еще нет ребенка с таким символом
            if (sym !in this) {
                // проверка на наличие дочернего массива
                if (children == null)
                    children = mutableListOf()
                // создание новой Node и добавление его в массив
                val newNode = Node(father = this, pchar = sym)
                children!!.add(Pair(newNode, sym))
                return newNode
            } else {
                // вернуть существующий Node
                return getNode(sym)!!
            }
        }

        /**
         * Возвращает символ от отца
         */
        fun getPerentChar(): Char? = pchar

        /**
         *  -Получение текста для состояния
         */
        fun getWord(): String {
            val string: String
            if (father == null)
                string = ""
            else
                string = father.getWord() + pchar
            return string
        }

        fun getWordLen(): Pair<Int, Int?> {
            val len: Int
            if (father == null) {
                len = 0
            } else {
                len = father.getWordLen().first + 1
            }
            return Pair(len, number)
        }

        override fun toString(): String {
            var result = "father is "
            result += "${father?.getWord() ?: "Null father"} ->${pchar}\n"
            result += "Terminal: ${terminal}\n"
            result += "Index in sample:${indexes?.joinToString(separator = " ")}\n"
            result += "Sub patterns:${subPatterns.joinToString(separator = "\t") { it.getWord() }}\n"
            result += "children of ${this.getWord()}:\n"
            if (children == null) result += "null"
            else children!!.forEach {
                result += "${it.second}-${it.first.getWord()}\t"
            }
            return result + "\n"
        }

        fun getChildren(): MutableList<Pair<Node, Char>>? = children

        fun allSubPatterns(): MutableSet<Node> {
            if (!flagSubPatterns) {// в массив subPatterns еще не были добавлены все внутренние паттерны
                sufflink?.let {
                    subPatterns.addAll(it.allSubPatterns())
                }
                flagSubPatterns = true
            }
            return subPatterns
        }
    }

    var root = Node()
    var totalString: Int = 0

    /**
     * Вставляет строку в бор
     * устанавливает значение для терминальной вершины
     */
    fun insert(string: String) {
        totalString++
        var currentNode = root
        string.forEachIndexed { index, c ->
            if (c !in currentNode) {
                currentNode.addChildren(c)
            }
            currentNode = currentNode.getNode(c)!!
        }
        currentNode.number = totalString
        currentNode.terminal = true
        currentNode.subPatterns.add(currentNode)
    }

    /**
     * Используется для построения суффиксных ссылок
     */
    fun go(node: Node, char: Char): Node {
        if (!node.go.contains(char)) {
            if (node.getNode(char) != null) {
                node.go[char] = node.getNode(char)!!
            } else if (node == root) { // перехода по ребру бора нет, находимся в корне
                node.go[char] = root
            } else { //нужно перейти по суффиксной ссылке и оттуда искать
                node.go[char] = go(sufflinkOf(node), char)
            }
        }
        return node.go[char]!!
    }

    /**
     * Находит суффиксную ссылку узла
     */
    private fun sufflinkOf(node: Node): Node {
        if (node.sufflink == null) {
            if (node == root || node.father == root) {
                node.sufflink = root
            } else {// переход по символу
                node.sufflink = go(sufflinkOf(node.father!!), node.getPerentChar()!!)
            }
        }
        return node.sufflink!!
    }

    /**
     * Возваращет массив пар:Номер слова, Индекс в тексте
     */
    fun getIndexesOf(text: String): List<Pair<Int, Int>> {
        val result = mutableSetOf<Pair<Int, Int>>()//формочка для вывода
        var node = root
        for (i in text.indices) {
            node = go(node, text[i]) //переход по суффискной ссылке
            var tmpNode = node
            while (tmpNode != root) {
                tmpNode = sufflinkOf(tmpNode)
            }


            node.allSubPatterns().forEach {
                val value = it.getWordLen()
                result.add(
                    Pair(i - value.first + 2, value.second!!)
                )
            }
        }
        return result.sortedWith(compareBy({ it.second }, { it.first }))
    }

    fun toString(node: Node = root, tab: Int = 0): String {
        var result = node.toString()
        node.getChildren()?.forEach {
            result += "\n"
            result += toString(it.first, tab + 1).prependIndent("\t".repeat(tab))
        }
        return result
    }
}


class Send {
    fun Send() {}

    @FXML
    private lateinit var txt: TextField

    @FXML
    private lateinit var patterns: TextField

    @FXML
    private lateinit var nodeInput: TextField

    @FXML
    private lateinit var terminalInput: TextField

    @FXML
    private lateinit var textField: TextField

    @FXML
    private lateinit var wrong: Text

    @FXML
    private lateinit var textText: Text

    @FXML
    private lateinit var patternsText: Text

    @FXML
    private lateinit var answerText: Text

    @FXML
    private lateinit var sendButton: Button

    @FXML
    private lateinit var addButton: Button

    @FXML
    private lateinit var switchButton: Button

    @FXML
    private lateinit var backButton: Button

    @FXML
    private lateinit var switchTextButton: Button

    @FXML
    private lateinit var trieImg: ImageView

    @FXML
    private lateinit var fsmImg: ImageView

    private lateinit var bor: Bor


    @FXML
    private fun sendData() {

        //wrong.text ="oops"
        if (txt.text.isNotEmpty() && patterns.text.isNotEmpty()) {
            //
            //println(txt.text)
            textField.isVisible = true
            switchTextButton.isVisible = true
            txt.isVisible = false
            patterns.isVisible = false
            patterns.isVisible = false
            sendButton.isVisible = false
            nodeInput.isVisible = true
            terminalInput.isVisible = true
            addButton.isVisible = true
            switchButton.isVisible = true
            trieImg.isVisible = true
            fsmImg.isVisible = true
            backButton.isVisible = true
            textText.isVisible = true
            patternsText.isVisible = true
            answerText.isVisible = true
            activateAlg()
        } else if (txt.getText().isEmpty()) wrong.text = "there is no text"
        else if (patterns.getText().isEmpty()) wrong.text = "there is no patterns"
    }

    @FXML
    private fun activateAlg() {

        textField.text = txt.text
        patternsText.text = "Patterns: ${patterns.text}"
        rebuildBor()
        answerText.text = "Answer \n${bor.getIndexesOf(txt.text).joinToString("\n") { "${it.second} ${it.first}" }}"

    }

    private fun rebuildBor() {
        bor = Bor()
        for (word in patterns.text.split('#')) {
            bor.insert(word)
        }
    }

    @FXML
    private fun switchText() {
        answerText.text =
            "Answer \n${bor.getIndexesOf(textField.getText()).joinToString("\n") { "${it.second} ${it.first}" }}"

    }

    //@FXML
    //private fun delNode(){
    //    val oldNodeText = nodeInput.getText()
    //    val fullNewNode: String = newNodeText[0] + newNodeText[1]
    //    if (fullNewNode !in patternsText.text) {
    //        patternsText.text += "#$fullNewNode"
    //        //patterns.text+="#$fullNewNode"
    //        bor = Bor()
    //        for (word in patterns.text.split('#')) {bor.insert(word)}
    //        patterns.text += "#$fullNewNode"
    //        bor.insert(fullNewNode)
    //        answerText.text =
    //            "Answer!! \n${bor.getIndexesOf(txt.text).joinToString("\n") { "${it.second} ${it.first}" }}"
    //    }
    //}
    @FXML
    private fun addNode() {
        val newNodeText = nodeInput.getText().toString().split("->")
        val fullNewNode: String = newNodeText[0] + newNodeText[1]
        if (fullNewNode !in patternsText.text) {
            patternsText.text += "#$fullNewNode"
            rebuildBor()
            //вывод картинки
            patterns.text += "#$fullNewNode"
            bor.insert(fullNewNode)
            //вывод картинки
            answerText.text =
                "Answer \n${bor.getIndexesOf(txt.text).joinToString("\n") { "${it.second} ${it.first}" }}"
        }
    }

    private fun deletePattern(str: String) {
        var tmpText = "#${patterns.text}#".replace("#${str}#", "#").replace("##", "#")
        if (tmpText.first() == '#') {
            tmpText = tmpText.substring(1)
        }
        if (tmpText.last() == '#') {
            tmpText = tmpText.substring(0, tmpText.length - 1)
        }
        patterns.text = tmpText
        patternsText.text = "Patterns: ${patterns.text}"
    }

    @FXML
    private fun goodSwitchTerminal() {
        var tmpNode = bor.root
        for (i in terminalInput.text) tmpNode = bor.go(tmpNode, i)
        tmpNode.terminal = !tmpNode.terminal
        if (tmpNode.terminal) {
            tmpNode.subPatterns.add(tmpNode)
            tmpNode.number = ++bor.totalString

            //tmpNode.number=patterns.text.count{it == '#' }+2
            //bor.totalString++

            patterns.text += "#${terminalInput.text}"
            patternsText.text = "Patterns: ${patterns.text}"
        } else {
            tmpNode.subPatterns.remove(tmpNode)
            tmpNode.number = null
            --bor.totalString
            deletePattern(terminalInput.text)
        }
        answerText.text =
            "Answer \n${bor.getIndexesOf(txt.text).joinToString("\n") { "${it.second} ${it.first}" }}"
    }

    @FXML
    private fun back() {
        txt.text = ""
        txt.isVisible = true
        patterns.text = ""
        patterns.isVisible = true
        sendButton.isVisible = true
        nodeInput.isVisible = false
        terminalInput.isVisible = false
        addButton.isVisible = false
        switchButton.isVisible = false
        trieImg.isVisible = false
        fsmImg.isVisible = false
        backButton.isVisible = false
        textField.isVisible = false
        switchTextButton.isVisible = false
        textText.isVisible = false
        patternsText.isVisible = false
        answerText.isVisible = false
    }
}