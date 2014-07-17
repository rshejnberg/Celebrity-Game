package Server;
import java.io.IOException;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class representing individual instances of the game that user's are playing
 * @author raphaelshejnberg
 *
 */
public class CelebrityGame {
	public Client user;
	private Node pos;
	private boolean timedOut = false;
	boolean gameInProgress;
	Display out;
	/**
	 * Constructor
	 * @param client
	 */
	public CelebrityGame(Client client) {
		user = client;
		out = new Display(user);
		pos = DTree.getInstance().getRoot();
		setStartPos();
		gameInProgress = false;
	}
	
	
	/**
	 * Prints out the whole tree
	 */
	public void printTree()  {
		try {
			out.printTree();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Triggers a call to traverse down the tree to the left child
	 */
	public void answerYes() {
		pos = DTree.getInstance().goLeft(pos);
	}
	/**
	 * Triggers a call to traverse down the tree to the right child
	 */
	public void answerNo() {
		pos = DTree.getInstance().goRight(pos);
	}
	/**
	 * Sets the node indicating the current position to the root of the tree
	 */
	private void setStartPos() {
		pos = DTree.getInstance().getRoot();
	}


	/**
	 * Gets the user response and handles the necessary actions to take based
	 * on that response.
	 * @throws IOException
	 */
	private void handleResponse() throws IOException {
		boolean answeredYes = out.StringToBool(askYesNoQuestion(null));
		if(answeredYes) {
			if(DTree.getInstance().isLeaf(pos))
				correctGuess();
			else
				answerYes();
		}
		else if(!DTree.getInstance().isLeaf(pos))
			answerNo();
		else
			addCelebrity();
	}
	/**
	 * Called when the game successfully guessed a client's celebrity. 
	 * Resets the game and notifies the submitter of the celebrity about this.
	 * @throws IOException
	 */
	private void correctGuess() throws IOException {
		user.write("I'm so smart" + out.nl);
		if(pos.creatorAddress != user.address)
			GameServer.serverKernel.alertSubmitterAboutGuess(pos, user.name);
		resetGame();
	}
	/**
	 * Resets game variables
	 */
	private void resetGame() {
		gameInProgress = false;
		timedOut = false;
		setStartPos();
	}
	private String readFromUser() {
		try {
		while(!timedOut && !user.availableForRead())
			Thread.sleep(1000);
		if(!timedOut)
			return user.getInput();
		} catch (InterruptedException e) {
			timedOut = true;
		}
		return null;
	}
	

	/**
	 * Asks the user a question and returns the response
	 * @param question
	 * @return
	 * @throws IOException
	 */
	private String askQuestion(String question) {
		user.write(question);
		return readFromUser();
	}
	
	
	/**
	 * Asks the user a question and gets a yes no response
	 * @param question
	 * @return
	 * @throws IOException
	 */
	private String askYesNoQuestion(String question)  {
		String response = null;
		if(question != null)
			user.write(question);
		while(!timedOut) {
			response = readFromUser();
			if(response.equals("yes") || response.equals("no") || response == null)
				break;
			user.write("Invalid input. Please enter 'yes' or 'no'.\n");
		}
		return response;
	}

	/**
	 * Retrieves all relevant information for the new celebrity
	 * @throws IOException
	 */
	private void addCelebrity() {
		Node lock;
		if(pos == DTree.getInstance().getRoot())
			lock = pos;
		else 
			lock = pos.parent;
		
    	synchronized(lock) {
		final ExecutorService service = Executors.newSingleThreadExecutor();
		try {

		    Runnable r = new Runnable() {
		        @Override
		        public void run() {
		        	//Lock tree at parent node of current node (unless currently at the root).
		    		
		        			String celebName = null, celebQuestion = null;
		        			boolean isQuestionTrueForNewCeleb = false;
		        			celebName = out.capitalize(askQuestion("I give up! Who were you thinking of?" + out.nl));
		        			if(!timedOut)
		        				celebQuestion = askQuestion("Ask a yes/no question that would distinguish between " + pos.content + " and " + celebName + out.nl);
		        			if(!timedOut)
		        				isQuestionTrueForNewCeleb = out.StringToBool(askYesNoQuestion("Would an answer of yes indicate " + celebName + out.nl));
							if(!timedOut) {
								user.write("Thank you for adding " + celebName + " to the database." + out.nl);
								celebQuestion = celebQuestion.trim();
								DTree.getInstance().addCelebrity(user, pos, celebName, celebQuestion, isQuestionTrueForNewCeleb);
							}
		    		}
		        
		    };
		    //Set the runnable task to timeout after 20 seconds
		    Future<?> evtWaiter = service.submit(r);
	 	    evtWaiter.get(20, TimeUnit.SECONDS);
		}
		catch (final InterruptedException | ExecutionException e) {
		    // The thread was interrupted during sleep, wait or join
		}
		catch (final TimeoutException e) {
		
			timedOut = true;
			service.shutdownNow();
			user.write("Game timed out. Starting over." + out.nl);
		   
		}
		finally {
		    
		    resetGame();
		}
    	}
	}

	/**
	 * Game menu loops until user say that they do not want to play.
	 * @throws IOException
	 */
	public void menu() {
		try {
			while(true) {
				if(out.StringToBool(askYesNoQuestion("Would you like to play the celebrity game?" + out.nl)))
					play();
				else {
					GameServer.serverKernel.clientDisconnect(user);
					break;
				}
			}
		} catch (IOException e) {
			System.err.println("IOException occured while in the menu");
			GameServer.serverKernel.clientDisconnect(user);
		} 
	}
	/**
	 * Game loop continues while gameInProgress is true.
	 * @throws NullPointerException 
	 * @throws IOException
	 */
	private void play() throws IOException  {
	    gameInProgress = true;
	    while(gameInProgress) {
				out.printNode(pos);
				handleResponse();			 
	    }
	    
	}
}
