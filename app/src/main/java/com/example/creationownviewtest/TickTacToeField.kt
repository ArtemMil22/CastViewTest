package com.example.creationownviewtest

enum class Cell {
    PLAYER_1,
    PLAYER_2,
    EMPTY
}
typealias OnFieldChangedListener = (field: TickTacToeField) -> Unit

//здесь будем хранить состояние ячеек
class TickTacToeField(
    val rows: Int,
    val columns: Int
) {
    private val cells = Array(rows) { Array(columns) { Cell.EMPTY } }

    val listener = mutableSetOf<OnFieldChangedListener>()

    fun getCell(row: Int, column: Int): Cell {
        if (row < 0 || column < 0 || row >= rows || column >= columns) return Cell.EMPTY
        return cells[row][column]
    }

    fun setCell(row: Int, column: Int, cell: Cell) {
        if (row < 0 || column < 0 || row >= rows || column >= columns) return
        if (cells[row][column] != cell) {
            cells[row][column] = cell
            listener?.forEach { it?.invoke(this) }
        }
    }
}