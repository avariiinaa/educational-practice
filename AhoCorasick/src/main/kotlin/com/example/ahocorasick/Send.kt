package com.example.ahocorasick

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.MutableNode
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.Text
import java.io.File

class Bor {
    /**
     * Класс вершины бора
     */
    class Node(
        var number: Int? = null,
        val father: Node? = null,
        protected val pchar: Char? = null
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
        fun getNode(ch: Char): Node? {
            if (children == null) { // нет потомков
                return null
            } else {// найти потомка к которому можно прийти по символу
                return children!!.firstOrNull {
                    it.second == ch
                }?.first
            }
        }


        /**
         * Возвращает объект Node в который переходят по
        символу-аргументу
         */

        fun addChildren(ch: Char): Node {
            // еще нет ребенка с таким символом
            if (ch !in this) {
                // проверка на наличие дочернего массива
                if (children == null)
                    children = mutableListOf()
                // создание новой Node и добавление его в массив
                val newNode = Node(father = this, pchar = ch)
                children!!.add(Pair(newNode, ch))
                return newNode
            } else {
                // вернуть существующий Node
                return getNode(ch)!!
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
            //result += "Index in sample:${indexes?.joinToString(separator = " ")}\n"
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
    private lateinit var delInput: TextField

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
    private lateinit var delButton: Button

    @FXML
    private lateinit var trieImg: ImageView

    @FXML
    private lateinit var fsmImg: ImageView

    private lateinit var bor: Bor
    //private lateinit var graph

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
            delInput.isVisible = true
            delButton.isVisible = true
            activateAlg()
        } else if (txt.getText().isEmpty()) wrong.text = "there is no text"
        else if (patterns.getText().isEmpty()) wrong.text = "there is no patterns"
    }

    class State(val name: String, val transitions: MutableMap<Char, State> = mutableMapOf())


    class AutomatonNode(val state: String, val transition: Char) {
        val children: MutableMap<Char, AutomatonNode> = mutableMapOf()
        var suffixLink: AutomatonNode? = null
        var outputLink: AutomatonNode? = null
        var isTrue: Boolean = false
    }

    private fun buildAutomaton(patterns: List<String>): AutomatonNode {
        val root = AutomatonNode(" ", ' ')

        for (pattern in patterns) {
            var currentNode = root

            for (char in pattern) {
                if (!currentNode.children.containsKey(char)) {
                    val newNode = AutomatonNode(currentNode.state + char, char)
                    currentNode.children[char] = newNode
                }

                currentNode = currentNode.children[char]!!
            }

            currentNode.outputLink = currentNode
            currentNode.isTrue = true
        }

        val queue = mutableListOf<AutomatonNode>()

        for (child in root.children.values) {
            child.suffixLink = root
            queue.add(child)
        }

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeAt(0)

            for (child in currentNode.children.values) {
                queue.add(child)

                var suffixNode = currentNode.suffixLink

                while (suffixNode != null && !suffixNode.children.containsKey(child.transition)) {
                    suffixNode = suffixNode.suffixLink
                }

                child.suffixLink = suffixNode?.children?.get(child.transition) ?: root
                child.outputLink = child.suffixLink?.outputLink ?: child.suffixLink
            }
        }

        return root
    }

    private fun markTrueNodes(root: AutomatonNode) {
        val visited = mutableSetOf<AutomatonNode>()

        fun dfs(node: AutomatonNode) {
            if (node.isTrue) {
                visited.add(node)
                return
            }

            if (visited.contains(node)) {
                return
            }

            visited.add(node)

            for (child in node.children.values) {
                dfs(child)
            }

            val suffixNode = node.suffixLink
            if (suffixNode != null) {
                dfs(suffixNode)
            }
        }

        dfs(root)
    }

    private fun saveTreAsPNG(root: AutomatonNode, filename: String, path: String = "") {
        val graph = Factory.mutGraph().setDirected(true)
        val nodes = mutableMapOf<AutomatonNode, MutableNode>()

        fun buildGraph(node: AutomatonNode) {
            val mutableNode = nodes.getOrPut(node) { Factory.mutNode(node.state) }
            if (node.isTrue) {
                mutableNode.add(Color.RED)
            }
            graph.add(mutableNode)

            for (child in node.children.values) {
                buildGraph(child)

                val childNode = nodes[child]!!
                val link = Factory.to(childNode).with(Label.of(child.transition.toString()))
                mutableNode.addLink(link)
            }
        }

        buildGraph(root)

        val dot = Graphviz.fromGraph(graph).render(Format.DOT).toString()
        val file = File(path, filename)
        val renderedGraph = Graphviz.fromString(dot).render(Format.PNG).toFile(file)
        println("Бор сохранен в файл: ${file.absolutePath}")
    }

    private fun buildAutomat(patterns: List<String>): AutomatonNode {
        val root = AutomatonNode(" ", ' ')

        for (pattern in patterns) {
            var currentNode = root

            for (char in pattern) {
                if (!currentNode.children.containsKey(char)) {
                    val newNode = AutomatonNode(currentNode.state + char, char)
                    currentNode.children[char] = newNode
                }

                currentNode = currentNode.children[char]!!
            }

            currentNode.outputLink = currentNode
        }

        val queue = mutableListOf<AutomatonNode>()

        for (child in root.children.values) {
            child.suffixLink = root
            queue.add(child)
        }

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeAt(0)

            for (child in currentNode.children.values) {
                queue.add(child)

                var suffixNode = currentNode.suffixLink

                while (suffixNode != null && !suffixNode.children.containsKey(child.transition)) {
                    suffixNode = suffixNode.suffixLink
                }

                child.suffixLink = suffixNode?.children?.get(child.transition) ?: root
                child.outputLink = child.suffixLink?.outputLink ?: child.suffixLink
            }
        }

        return root
    }

    private fun saveAutomatonAsPNG(automaton: AutomatonNode, filename: String) {
        val graph = Factory.mutGraph().setDirected(true)
        val nodes = mutableMapOf<AutomatonNode, MutableNode>()

        fun buildGraph(node: AutomatonNode) {
            val mutableNode = nodes.getOrPut(node) { Factory.mutNode(node.state) }
            graph.add(mutableNode)

            for (child in node.children.values) {
                buildGraph(child)

                val childNode = nodes[child]!!
                val link = Factory.to(childNode).with(Label.of(child.transition.toString()))
                mutableNode.addLink(link)
            }

            if (node.outputLink != null) {
                val outputLinkNode = nodes[node.outputLink!!]!!
                val outputLinkLink = Factory.to(outputLinkNode).with(Label.of("")).add(Color.BLUE)
                mutableNode.addLink(outputLinkLink)
            }

            if (node.suffixLink != null) {
                val suffixLinkNode = nodes[node.suffixLink!!]!!
                val suffixLinkLink = Factory.to(suffixLinkNode).with(Label.of("")).add(Color.GREEN)
                mutableNode.addLink(suffixLinkLink)
            }
        }

        buildGraph(automaton)

        val dot = Graphviz.fromGraph(graph).render(Format.DOT).toString()
        val relativePath1 = "src/main/resources/com/example/ahocorasick" // Относительный путь от текущей директории

        val path = File(System.getProperty("user.dir"), relativePath1).absolutePath
        val file = File(path, filename)
        val renderedGraph = Graphviz.fromString(dot).render(Format.PNG).toFile(file)
        println("Автомат сохранен в файл: $filename")
    }

    @FXML
    private fun activateAlg() {

        textText.text = "Text: "
        textField.text = txt.text
        patternsText.text = "Patterns: ${patterns.text}"
        rebuildBor()
        answerText.text = "Answer \n${bor.getIndexesOf(txt.text).joinToString("\n") { "${it.second} ${it.first}" }}"
        makePic()
    }
//
    private fun rebuildBor() {
        bor = Bor()
        for (word in patterns.text.split('#')) {
            bor.insert(word)
        }
    }

    private fun deletePattern(str: String) {
        var tmpText = "#${patterns.text}#".replace("#${str}#", "#").replace("##", "#")
        println(tmpText)
        if (tmpText.first() == '#') {
            tmpText = tmpText.substring(1)
        }
        if (tmpText.isNotEmpty() && tmpText.last() == '#') {
            tmpText = tmpText.substring(0, tmpText.length - 1)
        }
        patterns.text = tmpText
        patternsText.text = "Patterns: ${patterns.text}"
    }

    @FXML
    private fun switchText() {
        answerText.text =
            "Answer \n${bor.getIndexesOf(textField.getText()).joinToString("\n") { "${it.second} ${it.first}" }}"

    }

    @FXML
    private fun delNode() {
        val oldNode = delInput.text
        var pts = patterns.text.split("#").toMutableList()
        var i = 0
        while (i != pts.size) {
            if (oldNode in pts[i]) {
                println(i)
                println(pts)
                deletePattern(pts[i])
                pts.removeAt(i)
                i--
            }
            i++
        }
        rebuildBor()
        answerText.text =
            "Answer \n${bor.getIndexesOf(txt.text).joinToString("\n") { "${it.second} ${it.first}" }}"
        makePic()
    }
    fun makePic(){
        val patterns = "${patterns.text}"
        val automaton1 = buildAutomat(patterns.split("#"))
        saveAutomatonAsPNG(automaton1, "fsm.png")
        val patterns1 = patterns.split("#")
        val automaton2 = buildAutomaton(patterns1)
        markTrueNodes(automaton2)
        val filename1 = "trie.png"
        val relativePath1 = "src/main/resources/com/example/ahocorasick" // Относительный путь от текущей директории

        val path1 = File(System.getProperty("user.dir"), relativePath1).absolutePath

        saveTreAsPNG(automaton2, filename1, path1)
    }
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
        makePic()
    }


    @FXML
    private fun goodSwitchTerminal() {
        rebuildBor()
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
        makePic()
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

        delInput.isVisible = false
        delButton.isVisible = false
    }
}