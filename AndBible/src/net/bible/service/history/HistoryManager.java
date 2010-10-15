package net.bible.service.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.bible.android.currentpagecontrol.CurrentPageManager;

import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.passage.Key;

import android.util.Log;

public class HistoryManager {

	private static int MAX_HISTORY = 20;
	private Stack<VerseHistoryItem> history = new Stack<VerseHistoryItem>();
	private static HistoryManager singleton = new HistoryManager();

	private boolean isGoingBack = false;
	
	private static final String TAG = "HistoryManager";
	
	public static HistoryManager getInstance() {
		return singleton;
	}
	
	public boolean canGoBack() {
		return history.size()>1;
	}
	
	/**
	 *  called when a verse is changed
	 */
	public void pageChanged() {
		// if we cause the change by requesting Back then ignore it
		if (!isGoingBack) {
			//xxxtodo need to save document too so that can retrack to different doc type with different sort of key
			// but for now just track bible changes
			if (CurrentPageManager.getInstance().isBibleShown()) {
				Book doc = CurrentPageManager.getInstance().getCurrentPage().getCurrentDocument();
				Key verse = CurrentPageManager.getInstance().getCurrentPage().getKey();
				if (verse!=null) {
					Log.d(TAG, "Adding "+verse+" to history");
					VerseHistoryItem item = new VerseHistoryItem(doc, verse);
					add(history, item);
				}
			}
		}
	}
	
	//not used just keep a list of verse changes for now
	public void documentChanged() {
	}
	
	public void goBack() {
		if (history.size()>1) {
			try {
				Log.d(TAG, "History size:"+history.size());
				isGoingBack = true;
	
				// pop the current displayed verse 
				VerseHistoryItem currentItem = history.pop();
	
				// and go to previous item
				HistoryItem previousItem = history.peek();
				if (previousItem!=null) {
					Log.d(TAG, "Going back to:"+previousItem);
					previousItem.revertTo();
				}
			} finally {
				isGoingBack = false;
			}
		}
	}
	
	public List<HistoryItem> getHistory() {
		List<HistoryItem> allHistory = new ArrayList<HistoryItem>(history);
		return allHistory;
	}
	
	/** add item and check size of stack
	 * 
	 * @param stack
	 * @param item
	 */
	private void add(Stack<VerseHistoryItem> stack, VerseHistoryItem item) {
		// ensure no duplicates
		// if we don't do this then goBack() would cause the prev key to be re-added and we only ever can go back 1 place
		Log.d(TAG, "Stack size:"+stack.size());
		boolean removed = stack.removeElement(item);
		Log.d(TAG, "Found and removed:"+removed);
		
		stack.push(item);
		
		if (stack.size()>MAX_HISTORY) {
			Log.d(TAG, "Shrinking large stack");
			stack.setSize(MAX_HISTORY);
		}
	}
}
