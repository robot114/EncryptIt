package com.zsm.security;

import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.TreeSet;

public class SecurityUtility {

	static public void listAlgo() {
        TreeSet<String> algos = new TreeSet<String>();
        for (Provider p : Security.getProviders()) {
            for (Map.Entry<Object, Object> e : p.entrySet()) {
                String s = e.getKey().toString()
                    + " -> " + e.getValue().toString();
                if (s.startsWith("Alg.Alias.")) {
                    s = s.substring(10);
                }               
                algos.add(s);   
            }           
        }       
        for (String a : algos) {
            System.out.println(a);
        }   
	}
}
