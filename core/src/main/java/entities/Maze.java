package entities;

/**
 * Represents the game maze environment.
 *
 * Manages the layout grid, tile collision boundaries,
 * and dimensional queries for the game world.
 *
 * @author Mahammad Jafarli
 *
 */
public class Maze {

    // 2D grid storing wall ('#') and open path ('.') characters
    private char[][] maze;

    /**
     * Initializes the default 5x5 maze grid.
     */
    public Maze() {
        this.maze = new char[][] {
            {'#', '#', '#', '#', '#'},
            {'#', '.', '.', '.', '#'},
            {'#', '.', '#', '.', '#'},
            {'#', '.', '.', '.', '#'},
            {'#', '#', '#', '#', '#'}
        };
    }

    /**
     * Retrieves the underlying 2D character grid of the maze.
     *
     * @return a 2D char array representing the tile layout
     */
    public char[][] getMaze() {
        return maze;
    }

    /**
     * Replaces the current maze grid with a custom layout.
     *
     * @param maze the new 2D char array representing the map
     */
    public void setMaze(char[][] maze) {
        this.maze = maze;
    }

    /**
     * Returns the horizontal size of the maze grid.
     *
     * @return the number of rows or width units in the grid
     */
    public int getWidth() {
        return maze.length;
    }

    /**
     * Returns the vertical size of the maze grid.
     *
     * @return the number of columns or height units in the first row
     */
    public int getHeight() {
        return maze[0].length;
    }

    /**
     * Checks whether the tile at the specified coordinates is an impassable wall.
     *
     * @param x the horizontal grid index
     * @param y the vertical grid index
     * @return {@code true} if the target tile contains '#', {@code false} otherwise
     */
    public boolean isWall(int x, int y) {
        // Accesses grid as [row][column] where y is row index and x is column index
        return maze[y][x] == '#';
    }
}
