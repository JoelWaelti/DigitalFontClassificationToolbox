package ch.fhnw.ip5.digitalfontclassification.demos;

public class ChartLayoutInstructions {

    int rows;
    int columns;

    /**
     * Constructor
     *
     * @param rows    number of rows in the display/print grid
     * @param columns number of columns in the display/print grid
     */
    public ChartLayoutInstructions(int rows, int columns){
        this.rows = Math.abs(rows);
        this.columns = Math.abs(columns);
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = Math.abs(rows);
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = Math.abs(columns);
    }
}