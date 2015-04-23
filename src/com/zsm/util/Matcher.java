package com.zsm.util;

public interface Matcher<E, C> {
	/**
	 * Check if the element matches the condition
	 * 
	 * @param e the element
	 * @param condition the condition transfered by {@link #transferCondition(Object) }
	 * @return true, if the element matches the condition. false, otherwise.
	 */
	boolean match( E e, C transferedCondition );
	
	/**
	 * Check if the condition makes any element matched
	 * 
	 * @param condition the condition transfered by {@link #transferCondition(Object) }
	 * @return true, if the condition makes any element matched. false, otherwise.
	 */
	boolean matchAll( C transferedCondition );
	
	/**
	 * Transfer the condition to some converting format to use.
	 * @param originalCondition condition before bing transfered
	 * @return the transfered condition
	 */
	C transferCondition( C originalCondition );

	/**
	 * Do something when the condition has been checked and the user of Matcher
	 * has done what he need to do.
	 * 
	 * @param e the element been checked
	 * @param match whether the element match the condition
	 */
	void afterMatched( E e, boolean match );
}
