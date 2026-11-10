package entities;

public class Maze {
    private char[][]  maze;

    public Maze() {
        this.maze = new char[][] {
            {'#', '#', '#', '#', '#'},
            {'#', '.', '.', '.', '#'},
            {'#', '.', '#', '.', '#'},
            {'#', '.', '.', '.', '#'},
            {'#', '#', '#', '#', '#'}
        };
    }

    public char[][] getMaze() {
        return maze;
    }

    public void setMaze(char[][] maze) {
        this.maze = maze;
    }

    public int getWidth() {
        return maze.length;
    }
    public int getHeight() {
        return maze[0].length;
    }

    public boolean isWall(int x, int y) {
        return maze[y][x] == '#';
    }
}
