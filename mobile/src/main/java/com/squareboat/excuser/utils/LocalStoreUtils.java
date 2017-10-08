package com.squareboat.excuser.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareboat.excuser.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Vipul on 10/05/17.
 */

public class LocalStoreUtils {
    private static final String PREF_FILE_NAME = "com.squareboat.excuser";
    private static final String KEY_ONBOARDING_DATA = "onboarding_data";
    private static final String KEY_CONTACT_DATA = "contact_data";
    private static GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Gson gson = gsonBuilder.create();

    public static void setOnboardingCompleted(boolean value, Context context) {
        try {
            SharedPreferences.Editor editor = getSharedEditor(context);
            editor.putBoolean(KEY_ONBOARDING_DATA, value);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isOnboardingCompleted(Context context) {
        try {
            SharedPreferences pref = getSharedPreference(context);
            return pref.getBoolean(KEY_ONBOARDING_DATA, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setContacts(List<Contact> states, Context context) {
        try {
            SharedPreferences.Editor editor = getSharedEditor(context);
            Set<String> set = new HashSet<String>();
            if (states != null) {
                for (Contact state : states) {
                    set.add(gson.toJson(state));
                }
            }
            editor.putStringSet(KEY_CONTACT_DATA, set);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addContact(Contact contact, Context context) {
        List<Contact> contacts = new ArrayList<>();

        if (getContacts(context) != null) {
            contacts = getContacts(context);
        }

        contacts.add(contact);
        setContacts(contacts, context);
    }

    public static void updateContact(Contact contact, Context context) {
        List<Contact> contacts = getContacts(context);
        if (contacts != null) {
            for (int i = 0; i < contacts.size(); ++i) {
                if (contacts.get(i).getId() == contact.getId()) {
                    contacts.set(i, contact);
                    setContacts(contacts, context);
                }
            }
        }
    }

    public static void deleteContact(Contact contact, Context context) {
        List<Contact> contacts = getContacts(context);
        if (contacts != null) {
            for (int i = 0; i < contacts.size(); ++i) {
                if (contacts.get(i).getId() == contact.getId()) {
                    contacts.remove(i);
                    setContacts(contacts, context);
                }
            }
        }
    }

    public static List<Contact> getContacts(Context context) {
        List<Contact> contacts = null;
        try {
            SharedPreferences pref = getSharedPreference(context);

            Set<String> set = pref.getStringSet(KEY_CONTACT_DATA, Collections.EMPTY_SET);
            contacts = new ArrayList<>();

            if (set.isEmpty()) {
                contacts = null;
            } else {
                for (String s : set) {
                    Contact state = gson.fromJson(s, Contact.class);
                    contacts.add(state);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //sort contacts alphabetically
        if (contacts != null) {
            Collections.sort(contacts, new Comparator<Contact>() {
                @Override
                public int compare(Contact contact, Contact contact1) {
                    return contact.getName().compareToIgnoreCase(contact1.getName());
                }
            });
        }

        return contacts;
    }

    public static void clearSession(Context context) {
        try {
            SharedPreferences.Editor editor = getSharedEditor(context);
            editor.clear();
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SharedPreferences.Editor getSharedEditor(Context context)
            throws Exception {
        if (context == null) {
            throw new Exception("Context null Exception");
        }
        return getSharedPreference(context).edit();
    }

    private static SharedPreferences getSharedPreference(Context context)
            throws Exception {
        if (context == null) {
            throw new Exception("Context null Exception");
        }
        return context.getSharedPreferences(PREF_FILE_NAME, 0);
    }

}
