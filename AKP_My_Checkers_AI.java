package dkscheckerstournament;

import java.util.ArrayList;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * This is an AI that plays a game of checkers. The AI relies on a minmax algorithm to sift through all possible moves, assuming that I 
 * want to maximize my boardValue, and my opponent wants to minimize by boardValue. This way, I can minimize my opponent's ability to 
 * minimize my boardValue, which is determined by the heuristics in my boardValue method.
 * 
 * @author Andrew Peterson
 * @since 5/11/16
 */
public class AKP_My_Checkers_AI implements CheckersAI {
	
	/**
	 * @return name of the AI
	 */
	public String getName() {
		return "CheckerMaster 5000";
	}

	/**
	 * 
	 * Returns the "goodness" value of this board for this player
	 * 
	 * @param board is 8 x 8 board conditions
	 * 
	 * @param turn is the current turn (1 for RED -1 for BLACK). I ALWAYS pass my own turn into this method so that the boardValue
	 * will be appropriately small or big
	 * 
	 * @return the value of the board according to heuristics:
	 * 1. Edge pieces are better because they cannot be captured
	 * 2. Kings are better because they can move in all directions
	 * 3. If there are no opposing checkers, then return 1000 or -1000, depending on your color, to ensure that a winning
	 * 	state is a priority.
	 * 
	 * 
	 */
	public int boardValue (int[][] board, int turn) {
		int sum = 0;
		boolean win = true;
		boolean lose = true;
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (board[r][c] != turn) { //if you find an opponent checker, win is false
					win = false;
				}
				if (board[r][c] == turn) { //if you have a checker left, you haven't lost yet
					lose = false;
				}
				int pieceVal = board[r][c];
				if ((c == 0 || c == 7) && board[r][c] == turn) //a piece is more "good" if it is my piece and is on an edge
					pieceVal*=2;
				if (board[r][c] == 2*turn) //a king is more "good" than a normal checker
					pieceVal*=2;
				sum += pieceVal; //once pieceVal is determined, add it to the sum
			}
		}
		if (win) { //if there are no opponent checkers, then the boardValue is very good (a win is infinite goodness)
			return 1000;
		}	
		else if (lose) { //if you lost all of your checkers, then the boardValue is very bad (a loss is negative infinite goodness)
			return -1000;
		}
		return sum * turn;
	}
	
	/**
	 * This method returns an integer "rating" of a move. The rating of a move is inherited from the boardValues resulting from moves
	 * farther down the game tree, assuming that I will always maximize my boardValue, and my opponent will always minimize my boardValue.
	 * If the boardValues down one branch of the tree are low, then I cut off the recursive search through that branch in order to save time.
	 * I also cut off the recursive search if I find a win in the game tree.
	 * 
	 * @param board is condition of 8 x 8 checkerboard
	 * 
	 * @param Turn is the current turn. Turn switches from 1 to -1 back and forth for each recursion.
	 * 
	 * @param myTurn is the numerical value of my turn
	 * 
	 * @param recursions left before we evaluate the terminal nodes
	 * 
	 * @return the rating of a move
	 */
	public int getMinMaxRating (int[][] board, int myTurn, int currTurn, int recursions) {
		//BASE CASE: if there are no more recursions to be done, then return the boardValue resulting from the next turn, whether it is 
		//my turn or the opponent's turn. This is a terminal node.
		if (recursions == 0) {
			if (myTurn == currTurn) { //if it is my turn
				Move bestMove = getMaxMove (board, myTurn); //get the best move
				int [][] newBoard = makeMove (board, bestMove, currTurn); //get board after you make the best move
				return boardValue (newBoard, myTurn); //return value from best move
			}
			else { //if it is my opponent's turn
				Move bestMove = getMinMove (board, myTurn); //get the best move (for my opponent)
				int [][] newBoard = makeMove (board, bestMove, currTurn); //get board after opponent makes worst move
				return boardValue (newBoard, myTurn); //return value from worst move
			}
		}
		
		//This code block deals with the case if it is my turn, in which case the score is maximized
		else if (myTurn == currTurn) {
			int bestValue = -100000; //initialize bestValue with a very low number
			//for each of my possible moves, I need to see which inherits the highest rating from boardValues lower in the tree
			ArrayList<Move> allMoves = AIHelpers.getAllMoves (board, currTurn); //get all of my moves
			
			//pruning: if this node is obviously a bad choice (boardValues are low), then don't look farther down this node.
			//just return a low number so we don't go farther through this direction down the tree.
			if (boardValue (board, myTurn) <= -4)
				return -4;
			//if it looks like I can win by going down this node, then I definitely want to follow it
			if (boardValue (board, myTurn) == 1000)
				return 1000;
			
			for (Move m : allMoves) { //for each move, see what its inherited ranking is by moving down the game tree
				int [][] newBoard = makeMove (board, m, currTurn); //get new board after move has been made
				int nextNode = getMinMaxRating (newBoard, myTurn, currTurn*(-1), recursions - 1); //switch turn to opponent, 
				//tell method that one recursion has finished, and give method an updated board
			    bestValue = max (nextNode, bestValue); //get the value of the node that inherits the best value
			}
			return bestValue;
		}
		
		//This code block deals with the case if it is my opponent's turn, in which case the score is minimized
		else {
			int bestValue = 100000; //initialize bestValue with a very high number
			ArrayList<Move> allMoves = AIHelpers.getAllMoves (board, currTurn); //get all of my opponent's moves
			
			//pruning: if this node is obviously a bad choice (boardValues are low), then don't look farther down this node.
			//just return a low number so we don't go farther through this direction down the tree.
			if (boardValue (board, myTurn) <= -4)
				return -4;
			//if it looks like I can win by going down this node, then I definitely want to follow it
			if (boardValue (board, myTurn) == 1000)
				return 1000;
			
			for (Move m : allMoves) {
				int [][] newBoard = makeMove (board, m, currTurn); //get new board after opponent has made move
				int nextNode = getMinMaxRating (newBoard, myTurn, currTurn*(-1), recursions - 1); //change turn back to me,
				//tell method that one recursion has finished, and give method an updated board
				bestValue = min (nextNode, bestValue); //get value of node that inherits
			}
			return bestValue;
		}
	}
	
	/**
	 * Looks at all of the moves, and picks the best one according the minmax rating. If two or more moves have the same rating, then 
	 * one is chosen randomly.
	 * 
	 * @param board is the current board variable in CheckersGame
	 * 
	 * @param turn is the turn of the AI (1 for RED -1 for BLACK)
	 * 
	 * @return the "best" Move according to heuristics, which are determined by the Move's minmax rating
	 */
	
	public Move getMove(int[][] board, int turn) {
		ArrayList<Move> allMoves = AIHelpers.getAllMoves(board, turn);
		if (allMoves.size() > 0) {
			Move bestMove = allMoves.get (0);
			//get rating of this move
			int bestValue = getMinMaxRating (makeMove(board, bestMove, turn), turn, turn*-1, getNumberOfRecursions (board, turn)); 
			ArrayList<Integer> moveRatings = new ArrayList<Integer> (); //make an array to store the move ratings
		    moveRatings.add (bestValue); //store first rating
			for (int i = 1; i < allMoves.size(); i++) { //start at second move so that we do waste time with calculations
				int[][] nextBoard = makeMove (board, allMoves.get(i), turn); //get the board resulting from my move
				int nextVal = getMinMaxRating (nextBoard, turn, turn*-1, getNumberOfRecursions (board, turn)); //get rating for the move
				moveRatings.add (nextVal); //store the moveRating
				if (nextVal > bestValue) {
					bestMove = allMoves.get (i);
					bestValue = nextVal;
				}
			}
			ArrayList<Integer> IndecesOfBestMoves = new ArrayList<Integer> ();
			for (int i = 0; i < moveRatings.size(); i++) {
				if (moveRatings.get(i) == bestValue)
					IndecesOfBestMoves.add (i); //figure out which indices of moveRatings have ints that are the same as bestValue
			}
			int chosenMove = IndecesOfBestMoves.get ((int)(Math.random()*(IndecesOfBestMoves.size()))); //randomly get index of a best move
			bestMove = allMoves.get (chosenMove); 
			return bestMove;
		} 
		else {
			return null;
		}
	}
	
	
	/**
	 * Tells getMove how many recursions to make. The method makes this judgment according to how many of my checkers are on the board
	 * and how many of my opponent's checkers are on the board. Judgments are made made on the following criteria...
	 * 1. If there are 6 or fewer checkers on the board (endgame), look ahead 12.
	 * 2. If you are just starting the game, 22-24 checkers on board, look ahead 6
	 * 3. If you are in the middle of the game, 22-7 checkers on board, look ahead 7
	 * 
	 * @param board is the current 8 x 8 checker board
	 * 
	 * @param turn is the turn of the AI (1 for RED -1 for BLACK)
	 * 
	 * @return the number of recursions that getMove uses to call getMinMaxRating
	 */
	private int getNumberOfRecursions (int[][] board, int turn) {
		int myCount = 0; //my number of checkers
		int opponentCount = 0; //opponent's number of checkers
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (board[r][c]*turn > 0)
					myCount++;
				if (board[r][c]*turn < 0) 
					opponentCount++;
			}
		}
		int totalCount = myCount + opponentCount;
		if (totalCount >= 22)
			return 4;
		else if (totalCount > 6 && totalCount <= 22)
			return 5;
		else 
			return 6;	
	}
	
	
	/**
	 * returns move that my OPPONENT would make to MINIMIZE my score. Used in getMinMaxRating.
	 * 
	 * @param board is current 8 x 8 board conditions
	 * 
	 * @param turn is the current turn (1 for RED -1 for BLACK). Turn will always be passed in as MY turn.
	 * 
	 * @return My OPPONENT's currently possible move that minimizes my score
	 */
	public Move getMinMove (int[][] board, int turn) {
		ArrayList<Move> allMoves = AIHelpers.getAllMoves (board, -turn); //get all moves for my OPPONENT
		if (allMoves.size() > 0) {
			Move bestMove = allMoves.get(0);
			int bestValue = boardValue(makeMove (board, bestMove, -turn), turn); //Find MY boardValue when opponent makes move
			for (Move m : allMoves) {
				int[][] nextBoard = makeMove (board, m, -turn); //Get board when OPPONENT makes move
				int nextVal = boardValue (nextBoard, turn); //Get MY boardValue
				if (nextVal < bestValue) { //we end up with move that results in lowest boardValue
					bestMove = m;
					bestValue = nextVal;
				}
			}
			return bestMove;
		} else
			return null;
	}
	
	/**
	 * returns move that I would make to MAXIMIZE my score. Used in getMinMaxRating.
	 * 
	 * @param board is current 8 x 8 board conditions
	 * 
	 * @param turn is the current turn (1 for RED -1 for BLACK). Turn will always be passed in as MY turn.
	 * 
	 * @return My currently possible move that maximizes my score
	 */
	
	public Move getMaxMove (int[][] board, int turn) {
		ArrayList<Move> allMoves = AIHelpers.getAllMoves (board, turn); //get all of my moves
		if (allMoves.size() > 0) {
			Move bestMove = allMoves.get (0);
			int bestValue = boardValue (makeMove(board, bestMove, turn), turn); //get boardValue after my first possible move
			for (Move m : allMoves) {
				int[][] nextBoard = makeMove (board, m, turn); //get board after I make a possible move
				int nextVal = boardValue (nextBoard, turn); //get boardValue of that new board
				if (nextVal > bestValue) { //we end up with the move that result in the highest boardValue
					bestMove = m;
					bestValue = nextVal;
				}
			}
			return bestMove;
		} else
			return null;
	}
	
	/**
	 * @author ********D Sheldon*********
	 * @param board is a set of 8 x 8 board conditions
	 * @param m is a move. Can be null or not.
	 * @param turn
	 *            is the color of the player to get the valid moves for 1=RED,
	 *            -1=BLACK
	 * @return A new board with the Move made -- all jumped pieces removed, and
	 *         all new Kings upgraded
	 */
	
	static public int[][] makeMove(int[][] board, Move m, int turn) {
		int[][] newBoard = new int[8][8];
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++)
				newBoard[r][c] = board[r][c];
		if (m != null) { //***************************************added by Andrew
			Point lastH = m.hops.get(0);
			int piece = newBoard[lastH.row][lastH.col];
			for (Point p : m.hops) {
				if (turn == 1 && p.row == 7)
					piece = 2;
				if (turn == -1 && p.row == 0)
					piece = -2;
				newBoard[p.row][p.col] = piece;
				newBoard[lastH.row][lastH.col] = 0;
				if (Math.abs(lastH.row - p.row) == 2)
					newBoard[(lastH.row + p.row) / 2][(lastH.col + p.col) / 2] = 0;
				lastH = p;
			}
		}
		return newBoard;
	}
}





