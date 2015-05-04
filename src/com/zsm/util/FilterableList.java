package com.zsm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.zsm.log.Log;

/**
 * This class contains two lists. One is the original list, which holds all the
 * items. The other one just holds the items match the filter condition. The
 * original list is the super-collection of the matched list. While the matched
 * list is the sub-collection of the original list.
 * <p>The matched list is generated every time the method 
 * {@link FilterableList#filter} invoked.
 * <p>All the parameters "location" of the methods, are the location of the 
 * items in the matched list.
 * 
 * @author zsm
 *
 * @param <E> Type of elements in the list. 
 * @param <C> Type of condition to filter the list
 */
public class FilterableList<E, C> implements List<E> {
	
	final private List<E> originalList;
	final private List<E> matchedList;
	private C transferedCondition;
	private Matcher<E, C> matcher;

	public FilterableList( Matcher<E, C> m ) {
		this.matcher = m;
		transferedCondition = m.transferCondition( null );
		
		originalList = new ArrayList<E>();
		matchedList = new ArrayList<E>();
	}
	
	/**
	 * Add the object into the list at the location of the matched list.
	 * If the location is larger than or equals to the size of the matched list,
	 * the object will be add at the end of both lists. Even the location is less
	 * than the size of the original list. When the location is 0, the object
	 * will be added at the first location of both lists. For the location which
	 * is in the range ( 0, matchedList.size() ), the object will be added
	 * at the exact location of the matched list. But in the original list, it
	 * is at location just after the object at the location in the matched list.
	 * <p>For example, the original list is { 0, 1, 2, 3, 4, 5, 6, 7 }, and the 
	 * condition is odd number. That means the matched list is { 1, 3, 5, 7 }.
	 * When add 9 at location 2, the matched list is { 1, 3, 9, 5, 7 }. And the
	 * original list should be { 0, 1, 2, 3, 9, 4, 5, 6, 7 }.
	 * <p>And if the object does not match the condition, it will only be add
	 * into the original list at the location as above. For the example as above,
	 * when 10 is added, the matched list should not changed. The original list
	 * should be { 0, 1, 2, 3, 10, 4, 5, 6, 7 }.
	 * 
     * @throws IndexOutOfBoundsException
     *             when {@code location < 0 || location > matchedList.size()}
	 */
	@Override
	public void add(int location, E object) {
		originalList.add( locationInOriginalList(location), object );
		if( matched( object, transferedCondition ) ) {
			matchedList.add( location, object );
		}
	}

	private int locationInOriginalList(int location) {
		int ms = matchedList.size();
        if (location > ms || location < 0) {
        	throw new IndexOutOfBoundsException("Invalid index " + location
        										+ ", size is " + ms);
        }
		int i;
		if( location == 0 ) {
			i = 0;
		} else {
			if( location >= ms ) {
				i = originalList.size();
			} else {
				int j;
				for( i = 0, j = 0; j < location && i < originalList.size(); i++ ) {
					if( matched( originalList.get( i ), transferedCondition ) ) {
						j++;
					}
				}
				assert( j == location );
				assert( matchedList.get(j-1).equals( originalList.get( i-1 ) ) );
			}
		}
		return i;
	}

	@Override
	public boolean add(E object) {
		originalList.add( object );
		if( matched( object, transferedCondition ) ) {
			matchedList.add( object );
		}
		return true;
	}

	/**
	 * Add all the items in the parameter "collection" at the "location" of the
	 * original list. All the matched items will be also added at the location
	 * of the matched list. Refer to {@link #add(int, Object)} for the 
	 * description of "location".
	 * 
	 * @return true if the <b>matched list</b> has been modified through the insertion,
	 * 			false otherwise (i.e. if the passed collection was empty,
	 * 							 or no matched item to the matched list).
	 * @see #add(int, Object)
	 */
	@Override
	public boolean addAll(int location, Collection<? extends E> collection) {
		originalList.addAll( locationInOriginalList(location), collection );
		int newLocation = location;
		for( E e : collection ) {
			if( matched( e, transferedCondition ) ) {
				matchedList.add( newLocation, e );
				newLocation++;
			}
		}
		
		return location < newLocation;
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		return addAll( matchedList.size(), collection );
	}

	/**
	 * Both lists are cleared.
	 */
	@Override
	public void clear() {
		originalList.clear();
		matchedList.clear();
	}

	/**
	 * Tests whether the <b>matched list</b> contains the specified object.
	 * 
	 * @param object the object to search for.
	 * @return true if object is an element of the matched list, false otherwise
	 * @see #containsInOriginal(Object)
	 */
	@Override
	public boolean contains(Object object) {
		return false;
	}

	/**
	 * Tests whether the <b>original list</b> contains the specified object.
	 * 
	 * @param object the object to search for.
	 * @return true if object is an element of the original list, false otherwise
	 * @see #contains(Object)
	 */
	public boolean containsInOriginal(Object object) {
		return false;
	}
	
	/**
	 * Tests whether the <b>matched list</b> contains all objects contained in the
     * specified collection.
	 * 
	 * @param collection
     *            the collection of objects
	 * @return {@code true} if all objects in the specified collection are
     *         elements of the matched list, {@code false} otherwise.
	 * @see #containsAllInOriginal(Collection<?>)
	 */
	@Override
	public boolean containsAll(Collection<?> collection) {
		return matchedList.containsAll(collection);
	}

	/**
	 * Tests whether the <b>matched list</b> contains all objects contained in the
     * specified collection.
	 * 
	 * @param collection
     *            the collection of objects
	 * @return {@code true} if all objects in the specified collection are
     *         elements of the matched list, {@code false} otherwise.
	 * @see #containsAll(Collection<?>)
	 */
	public boolean containsAllInOriginal(Collection<?> collection) {
		return originalList.containsAll(collection);
	}
	
	/**
	 * Returns the element at the specified location in the <b>matched list</b>.
	 */
	@Override
	public E get(int location) {
		return matchedList.get(location);
	}

	/**
	 * Searches <b>matched list</b> for the specified object and returns the
	 * index of the first occurrence.
	 */
	@Override
	public int indexOf(Object object) {
		return matchedList.indexOf(object);
	}

	/**
	 * Returns whether <b>matched list</b> contains no elements.
	 */
	@Override
	public boolean isEmpty() {
		return matchedList.isEmpty();
	}

	/**
	 * Returns an iterator on the elements of the <b>matched list</b>.
	 * The elements are iterated in the same order as they occur in the
	 * matched list.
	 * 
	 * @return an iterator on the elements of <b>matched list</b>.
	 */
	@Override
	public Iterator<E> iterator() {
		return matchedList.iterator();
	}

	/**
	 * Searches <b>matched list</b> for the specified object and returns the
	 * index of the last occurrence.
	 */
	@Override
	public int lastIndexOf(Object object) {
		return matchedList.lastIndexOf(object);
	}

	/**
	 * Returns a List iterator on the elements of the <b>matched list</b>. The elements 
	 * are iterated in the same order that they occur in the List.
	 */
	@Override
	public ListIterator<E> listIterator() {
		return matchedList.listIterator();
	}

	/**
	 * Returns a list iterator on the elements of the <b>matched list</b>. The elements are
	 * iterated in the same order as they occur in the List. The iteration
	 * starts at the specified location.
	 */
	@Override
	public ListIterator<E> listIterator(int location) {
		return matchedList.listIterator(location);
	}

	/**
	 * Removes the object at the specified location from the <b>matched list</b>.
	 * The related object in the original list will be removed too.
	 * For example, the original list is { 0, 1, 2, 3, 3, 4, 5 }. And the 
	 * condition is odd number. So the matched list should be { 1, 3, 3, 5 }.
	 * When the location is 2, the second 3 in both lists will be removed.
	 * The first 3 will be still in both list.
	 */
	@Override
	public E remove(int location) {
		originalList.remove( locationInOriginalList( location ) );
		return matchedList.remove(location);
	}

	/**
	 * Removes the first occurrence of the specified object from the 
	 * <b>matched list</b>. If the object is not in the matched list,
	 * but in the original list, the object will not be removed from
	 * the original list either, and false will be returned.
	 * 
	 * @return true if the <b>matched list</b> was modified by this operation,
	 * 		   false otherwise.
	 */
	@Override
	public boolean remove(Object object) {
		Log.d( "Item to be removed from both lists.", object );
		if( matchedList.remove( object ) ) {
			originalList.remove(object);
			Log.d( "Item removed from both lists.", object );
			return true;
		}
		return false;
	}

	/**
	 * Removes all occurrences in the <b>matched list</b> of each
	 * object in the specified collection. 
	 * <p>If the object in collection is not in the matched list,
	 * but in the original list, the object will not be removed from
	 * the original list either.
	 * 
	 * @return true if the <b>matched list</b> was modified by this operation,
	 * 		   false otherwise.
	 */
	@Override
	public boolean removeAll(Collection<?> collection) {
        boolean result = false;
        Iterator<?> it = matchedList.iterator();
        Iterator<?> oit = originalList.iterator();
        while (it.hasNext()) {
        	Object obj = it.next();
            if (collection.contains(obj)) {
            	Object oo;
            	do {
            		oo = oit.next();
            	} while( !oo.equals(obj) );
                oit.remove();
                it.remove();
                result = true;
            }
        }
        return result;
	}

	/**
	 * Removes all objects from the <b>matched list</b> that are not contained
	 * in the specified collection. The original list will be modified too. 
	 * For example, the original list is { 0, 1, 2, 3, 3, 4, 5 }. And the 
	 * condition is odd number. So the matched list should be { 1, 3, 3, 5 }.
	 * The collection is { 1, 2, 3, 4, 7 }. After this method invoked, the original
	 * list should be { 1, 2, 3, 3, 4 }, and the matched list should be { 1, 3, 3 }.
	 * 
	 * @return true if the <b>matched list</b> was modified by this operation,
	 * 		   false otherwise.
	 */
	@Override
	public boolean retainAll(Collection<?> collection) {
		originalList.retainAll(collection);
        return matchedList.retainAll(collection);
	}

	/**
	 * Replaces the element at the specified location in the <b>matched list</b>
	 * with the specified object. This operation does not change the size of
	 * the List. The related element in the original will be replaced.
	 */
	@Override
	public E set(int location, E object) {
		Log.d( "Item in list to be modified.", "position", location,
			   "new item", object );
		originalList.set( locationInOriginalList( location ), object );
		return matchedList.set(location, object);
	}

	/**
	 * Returns the number of elements in the <b>matched list</b>.
	 */
	@Override
	public int size() {
		return matchedList.size();
	}

	/**
	 * Returns the number of elements in the <b>original list</b>.
	 * 
	 * @return the number of elements in the <b>original list</b>.
	 */
	public int totalSize() {
		return originalList.size();
	}
	
	/**
	 * Returns a List of the specified portion of the <b>matched list</b> from the given 
	 * start index to the end index minus one. The returned List is backed 
	 * by the <b>matched list</b> so changes to it are reflected by the other.
	 */
	@Override
	public List<E> subList(int start, int end) {
		return matchedList.subList(start, end);
	}

	/**
	 * Returns an array containing all elements contained in the <b>matched list</b>.
	 */
	@Override
	public Object[] toArray() {
		matchedList.toArray();
		return null;
	}

	/**
	 * Returns an array containing all elements contained in the <b>matched list</b>. 
	 * If the specified array is large enough to hold the elements, 
	 * the specified array is used, otherwise an array of the same type 
	 * is created. If the specified array is used and is larger than this 
	 * List, the array element following the collection elements is set to null.
	 */
	@Override
	public <T> T[] toArray(T[] array) {
		return matchedList.toArray( array );
	}
	
	public void filter( C condition ) {
		this.transferedCondition = matcher.transferCondition( condition );
		filterWithTransferedCondition();
	}

	private void filterWithTransferedCondition() {
		matchedList.clear();
		if( matcher.matchAll(this.transferedCondition) ) {
			matchedList.addAll(originalList);
		} else {
			for( E e : originalList ) {
				if( matched( e, this.transferedCondition ) ) {
					matchedList.add(e);
					matcher.afterMatched( e, true );
				} else {
					matcher.afterMatched( e, false );
				}
			}
		}
	}
	
	public void refilter() {
		filterWithTransferedCondition();
	}
	
	private boolean matched( E e, C condition ) {
		return matcher.match( e, condition );
	}

}
