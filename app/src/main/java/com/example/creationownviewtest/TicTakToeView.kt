package com.example.creationownviewtest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import java.lang.Integer.max
import kotlin.math.floor
import kotlin.properties.Delegates

typealias OnCellActionListener = (row: Int, column: Int, field: TickTacToeField) -> Unit

//так как вью с нуля создаем, значит и наследуемся от вью
@SuppressLint("ObsoleteSdkInt")
class TicTakToeView(
    context: Context,
    attributesSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attributesSet, defStyleAttr, defStyleRes) {

    var tickTacToeField: TickTacToeField? = null
        set(value) {
            field?.listener?.remove(listeners)
            field = value
            field?.listener?.add(listeners)
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    // назначаем здесь своего слушателя, чтобы слушать
    // действия пользователя с этим представлением в вашей активности
    var actionListener: OnCellActionListener? = null

    private var player1Color by Delegates.notNull<Int>()
    private var player2Color by Delegates.notNull<Int>()
    private var gridColor by Delegates.notNull<Int>()

    // прямоугольник безопасной зоны, где мы можем рисовать
    private val fieldRect = RectF(0f, 0f, 0f, 0f)

    // размер одной ячейки
    private var cellSize: Float = 0f

    // padding в ячейке
    private var cellPadding: Float = 0f

    // вспомогательная переменная, чтобы избежать выделения объекта в onDraw
    private val cellRect = RectF()

    // текущая выбранная ячейка, полезно, когда устройство не имеет сенсорного экрана
    private var currentRow: Int = -1
    private var currentColumn: Int = -1

    // предварительно инициализированные кисти для рисования (стиль)
    private lateinit var player1Paint: Paint
    private lateinit var player2Paint: Paint
    private lateinit var currentCellPaint: Paint
    private lateinit var gridPaint: Paint

    constructor(
        context: Context, attributesSet: AttributeSet?, defStyleAttr: Int
    ) : this(context, attributesSet, defStyleAttr, R.style.DefaultTicTacToeFieldStyle)

    constructor(
        context: Context, attributesSet: AttributeSet?
    ) : this(context, attributesSet, R.attr.ticTacToeFieldStyle)

    constructor(context: Context) : this(context, null)

    init {
        if (attributesSet != null) {
            initAttributes(attributesSet, defStyleAttr, defStyleRes)
        } else {
            initDefaultColors()
        }
        initPaints()

        // здесь мы можем инициализировать некоторые данные для
        // предварительного просмотра компонента в Android Studio.
        if (isInEditMode) {

            tickTacToeField = TickTacToeField(8, 6)
            tickTacToeField?.setCell(1, 2, Cell.PLAYER_1)
            tickTacToeField?.setCell(6, 5, Cell.PLAYER_2)
            tickTacToeField?.setCell(2, 3, Cell.PLAYER_1)
            tickTacToeField?.setCell(5, 4, Cell.PLAYER_2)
            tickTacToeField?.setCell(2, 3, Cell.PLAYER_1)
            tickTacToeField?.setCell(4, 5, Cell.PLAYER_2)

        }

        // заставить наше view работать на устройствах без сенсорного экрана
        isFocusable = true
        isClickable = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            defaultFocusHighlightEnabled = false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        tickTacToeField?.listener?.add(listeners)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tickTacToeField?.listener?.remove(listeners)
    }

    private fun initPaints() {
        // кисть для рисования X
        player1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        player1Paint.color = player1Color
        player1Paint.style = Paint.Style.STROKE
        player1Paint.strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)

        // кисть для рисования O
        player2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        player2Paint.color = player2Color
        player2Paint.style = Paint.Style.STROKE
        player2Paint.strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)

        // кисть для рисования сетка
        gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        gridPaint.color = gridColor
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)

        // кисть для рисования текущей ячейки
        currentCellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        currentCellPaint.color = Color.rgb(230, 230, 230)
        currentCellPaint.style = Paint.Style.FILL
    }

    private fun initAttributes(
        attributesSet: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val typedArray = context.obtainStyledAttributes(
            attributesSet,
            R.styleable.TicTakToeView,
            defStyleAttr,
            defStyleRes
        )

        player1Color =
            typedArray.getColor(R.styleable.TicTakToeView_player1Color, PLAYER1_DEFAULT_COLOR)
        player2Color =
            typedArray.getColor(R.styleable.TicTakToeView_player2Color, PLAYER2_DEFAULT_COLOR)
        gridColor = typedArray.getColor(R.styleable.TicTakToeView_gridColor, GRID_DEFAULT_COLOR)

        typedArray.recycle()
    }

    private fun initDefaultColors() {
        player1Color = PLAYER1_DEFAULT_COLOR
        player2Color = PLAYER2_DEFAULT_COLOR
        gridColor = GRID_DEFAULT_COLOR
    }

    //предложение компановщика и программист договариваеся
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        //минимальный размер нашего представления
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        //вычисление желаемого размера представления
        val desiredCellSizeInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DESIRED_CELL_SIZE,
            resources.displayMetrics
        ).toInt()
        val rows = tickTacToeField?.rows ?: 0
        val columns = tickTacToeField?.columns ?: 0
        // желаемый размер
        val desiredWith =
            max(minWidth, columns * desiredCellSizeInPixels + paddingLeft + paddingRight)
        val desiredHeight =
            max(minHeight, rows * desiredCellSizeInPixels + paddingTop + paddingBottom)
        //отправить размер view
        setMeasuredDimension(
            resolveSize(desiredWith, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //реальный размер представления после всех расчетов
        //когда размер представления изменяется, нам нужно пересчитывать безопасную зону и размер ячейки
        updateViewSizes()
    }

    private fun updateViewSizes() {
        val field = this.tickTacToeField ?: return

        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom

        val cellWidth = safeWidth / field.columns.toFloat()
        val cellHeight = safeHeight / field.rows.toFloat()

        cellSize = kotlin.math.min(cellWidth, cellHeight)
        cellPadding = cellSize * 0.2f

        val fieldWidth = cellSize * field.columns
        val fieldHeight = cellSize * field.rows

        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }

    // раздел обработки событий

    // обработка действий от клавиатуры
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> moveCurrentCell(1, 0)
            KeyEvent.KEYCODE_DPAD_LEFT -> moveCurrentCell(0, -1)
            KeyEvent.KEYCODE_DPAD_RIGHT -> moveCurrentCell(0, 1)
            KeyEvent.KEYCODE_DPAD_UP -> moveCurrentCell(-1, 0)
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // GestureDetector скролинг и изменение масштаба у нас тоьлко нажатие
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                updateCurrentCell(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                updateCurrentCell(event)
                return true
            }
            MotionEvent.ACTION_UP -> {
                return performClick()
            }
        }
        return false
    }

    // получаем строку из ивента
    private fun getRow(event: MotionEvent): Int {
        // в нашем случае пол лучше, чем простое округление до целого числа
        // потому что он округляется до целого числа в сторону отрицательной бесконечности
        // пример:
        // 1) -0.3.toInt() = 0
        // 2) floor(-0.3) = -1
        return floor((event.y - fieldRect.top) / cellSize).toInt()
    }

    // получаем колонку из ивента
    private fun getColumn(event: MotionEvent): Int {
        return floor((event.x - fieldRect.left) / cellSize).toInt()
    }

    private fun updateCurrentCell(event: MotionEvent) {
        val field = this.tickTacToeField ?: return
        val row = getRow(event)
        val column = getColumn(event)
        if (row >= 0 && column >= 0 && row < field.rows && column < field.columns) {
            if (currentRow != row || currentColumn != column) {
                currentRow = row
                currentColumn = column
                invalidate()
            }
        } else {
            // очистка текущей ячейки, если пользователь выходит из представления
            currentRow = -1
            currentColumn = -1
            invalidate()
        }
    }

    // пол
    override fun performClick(): Boolean {
        super.performClick()
        val field = this.tickTacToeField ?: return false
        val row = currentRow
        val column = currentColumn
        if (row >= 0 && column >= 0 && row < field.rows && column < field.columns) {
            actionListener?.invoke(row, column, field)
            return true
        }
        return false
    }

    private fun moveCurrentCell(rowDiff: Int, columnDiff: Int): Boolean {
        val field = this.tickTacToeField ?: return false
        if (currentRow < 0 || currentColumn < 0 || currentRow >= field.rows
            || currentColumn >= field.columns
        ) {
            currentRow = 0
            currentColumn = 0
            invalidate()
            return true
        } else {
            if (currentColumn + columnDiff < 0) return false
            if (currentColumn + columnDiff >= field.columns) return false
            if (currentRow + rowDiff < 0) return false
            if (currentRow + rowDiff >= field.rows) return false

            currentColumn += columnDiff
            currentRow += rowDiff
            invalidate()
            return true
        }
    }

    // раздел чертежа, максималльно минимализирован должен, нужно определить кисти

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (tickTacToeField == null) return
        if (cellSize == 0f) return
        if (fieldRect.width() <= 0) return
        if (fieldRect.height() <= 0) return

        drawGrid(canvas)
        drawCurrentCell(canvas)
        drawCells(canvas)
    }

    //рисуем текущую ячейку, мало ли нет сенсора у человечка
    private fun drawCurrentCell(canvas: Canvas) {
        val field = this.tickTacToeField ?: return
        if (currentRow < 0 && currentColumn < 0 && currentRow >= field.rows
            && currentColumn >= field.columns
        ) return

        val cell = getCellRect(currentRow, currentColumn)
        canvas.drawRect(
            cell.left - cellPadding,
            cell.top - cellPadding,
            cell.right + cellPadding,
            cell.bottom + cellPadding,
            currentCellPaint
        )
    }

    private fun drawGrid(canvas: Canvas) {
        val field = this.tickTacToeField ?: return

        val xStart = fieldRect.left
        val xEnd = fieldRect.right
        for (i in 0..field.rows) {
            val y = fieldRect.top + cellSize * i
            canvas.drawLine(xStart, y, xEnd, y, gridPaint)
        }

        val yStart = fieldRect.top
        val yEnd = fieldRect.bottom
        for (i in 0..field.columns) {
            val x = fieldRect.left + cellSize * i
            canvas.drawLine(x, yStart, x, yEnd, gridPaint)
        }
    }

    private fun drawCells(canvas: Canvas) {
        val field = this.tickTacToeField ?: return
        for (row in 0 until field.rows) {
            for (column in 0 until field.columns) {
                val cell = field.getCell(row, column)
                if (cell == Cell.PLAYER_1) {
                    drawPlayer1(canvas, row, column)
                } else if (cell == Cell.PLAYER_2) {
                    drawPlayer2(canvas, row, column)
                }
            }
        }
    }

    private fun drawPlayer1(canvas: Canvas, row: Int, column: Int) {
        val cellRect = getCellRect(row, column)
        canvas.drawLine(cellRect.left, cellRect.top, cellRect.right, cellRect.bottom, player1Paint)
        canvas.drawLine(cellRect.right, cellRect.top, cellRect.left, cellRect.bottom, player1Paint)
    }

    private fun drawPlayer2(canvas: Canvas, row: Int, column: Int) {
        val cellRect = getCellRect(row, column)
        canvas.drawCircle(
            cellRect.centerX(),
            cellRect.centerY(),
            cellRect.width() / 2,
            player2Paint
        )
    }

    // вспомогательные методы

    // вычисление координат по строке и по колонке
    private fun getCellRect(row: Int, column: Int): RectF {
        cellRect.left = fieldRect.left + column * cellSize + cellPadding
        cellRect.top = fieldRect.top + row * cellSize + cellPadding
        cellRect.right = cellRect.left + cellSize - cellPadding * 2
        cellRect.bottom = cellRect.top + cellSize - cellPadding * 2
        return cellRect
    }

    // при изменении некоторых данных в поле -> нужно перерисовать вид
    private val listeners: OnFieldChangedListener = {
        invalidate()
    }

    class SavedState : BaseSavedState {

        var currentRow by Delegates.notNull<Int>()
        var currentColumn by Delegates.notNull<Int>()

        constructor(superState: Parcelable) : super(superState)
        constructor(parcel: Parcel) : super(parcel) {
            currentRow = parcel.readInt()
            currentColumn = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentRow)
            out.writeInt(currentColumn)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = Array(size) { null }
            }
        }

    }

    companion object {
        const val PLAYER1_DEFAULT_COLOR = Color.GREEN
        const val PLAYER2_DEFAULT_COLOR = Color.RED
        const val GRID_DEFAULT_COLOR = Color.GRAY

        const val DESIRED_CELL_SIZE = 50f
    }
}