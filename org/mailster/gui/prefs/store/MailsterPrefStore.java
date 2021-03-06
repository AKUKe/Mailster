/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.mailster.gui.prefs.store;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.mailster.gui.prefs.ConfigurationManager;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * MailsterPrefStore.java - A concrete preference store implementation based on 
 * an internal <code>OrderedProperties</code> object, with support for 
 * persisting the non-default preference values to files or streams.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author: kods $ / $Date: 2009/05/17 20:56:35 $
 */
/**
 * A concrete preference store implementation based on an internal
 * <code>OrderedProperties</code> object, with support for persisting the
 * non-default preference values to files or streams.
 * <p>
 * This class was not designed to be subclassed.
 * </p>
 * 
 * @see org.eclipse.jface.preference.IPreferenceStore
 */
public class MailsterPrefStore extends EventManager implements
        IPersistentPreferenceStore 
{
	/**
	 * The header inserted at top of the properties file.
	 */
	public final static String HEADER = ConfigurationManager.MAILSTER_VERSION+" properties file";
	
    /**
     * The mapping from preference name to preference value (represented as
     * strings).
     */
    private OrderedProperties properties;

    /**
     * The mapping from preference name to default preference value (represented
     * as strings); <code>null</code> if none.
     */
    private OrderedProperties defaultProperties;

    /**
     * Indicates whether a value as been changed by <code>setToDefault</code>
     * or <code>setValue</code>; initially <code>false</code>.
     */
    private boolean dirty = false;

    /**
     * The file name used by the <code>load</code> method to load a property
     * file. This filename is used to save the properties file when
     * <code>save</code> is called.
     */
    private String filename;

    /**
     * Creates an empty preference store.
     * <p>
     * Use the methods <code>load(InputStream)</code> and
     * <code>save(InputStream)</code> to load and store this preference store.
     * </p>
     * 
     * @see #load(InputStream)
     * @see #save(OutputStream, String)
     */
    private MailsterPrefStore() {
        defaultProperties = new OrderedProperties();
        properties = new OrderedProperties(defaultProperties);
    }

    /**
     * Creates an empty preference store that loads from and saves to the a
     * file.
     * <p>
     * Use the methods <code>load()</code> and <code>save()</code> to load
     * and store this preference store.
     * </p>
     * 
     * @param filename
     *            the file name
     * @see #load()
     * @see #save()
     */
    public MailsterPrefStore(String filename) {
        this();        
        this.filename = filename;
    }
    
    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        addListenerObject(listener);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean contains(String name) {
        return (properties.containsKey(name) || defaultProperties
                .containsKey(name));
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void firePropertyChangeEvent(String name, Object oldValue,
            Object newValue) {
        final Object[] finalListeners = getListeners();
        // Do we need to fire an event.
        if (finalListeners.length > 0
                && (oldValue == null || !oldValue.equals(newValue))) {
            final PropertyChangeEvent pe = new PropertyChangeEvent(this, name,
                    oldValue, newValue);
            for (int i = 0; i < finalListeners.length; ++i) {
                final IPropertyChangeListener l = (IPropertyChangeListener) finalListeners[i];
                SafeRunnable.run(new SafeRunnable(JFaceResources.getString("PreferenceStore.changeError")) { //$NON-NLS-1$
                        public void run() {
                            l.propertyChange(pe);
                        }
                });
            }
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean getBoolean(String name) {
        return getBoolean(properties, name);
    }

    /**
     * Helper function: gets boolean for a given name.
     * 
     * @param p
     * @param name
     * @return boolean
     */
    private boolean getBoolean(Properties p, String name) {
        String value = p != null ? p.getProperty(name) : null;
        if (value == null) {
            return BOOLEAN_DEFAULT_DEFAULT;
        }
        if (value.equals(IPreferenceStore.TRUE)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean getDefaultBoolean(String name) {
        return getBoolean(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public double getDefaultDouble(String name) {
        return getDouble(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public float getDefaultFloat(String name) {
        return getFloat(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public int getDefaultInt(String name) {
        return getInt(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public long getDefaultLong(String name) {
        return getLong(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public String getDefaultString(String name) {
        return getString(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public double getDouble(String name) {
        return getDouble(properties, name);
    }

    /**
     * Helper function: gets double for a given name.
     * 
     * @param p
     * @param name
     * @return double
     */
    private double getDouble(Properties p, String name) {
        String value = p != null ? p.getProperty(name) : null;
        if (value == null) {
            return DOUBLE_DEFAULT_DEFAULT;
        }
        double ival = DOUBLE_DEFAULT_DEFAULT;
        try {
            ival = new Double(value).doubleValue();
        } catch (NumberFormatException e) {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public float getFloat(String name) {
        return getFloat(properties, name);
    }

    /**
     * Helper function: gets float for a given name.
     * @param p
     * @param name
     * @return float
     */
    private float getFloat(Properties p, String name) {
        String value = p != null ? p.getProperty(name) : null;
        if (value == null) {
            return FLOAT_DEFAULT_DEFAULT;
        }
        float ival = FLOAT_DEFAULT_DEFAULT;
        try {
            ival = new Float(value).floatValue();
        } catch (NumberFormatException e) {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public int getInt(String name) {
        return getInt(properties, name);
    }

    /**
     * Helper function: gets int for a given name.
     * @param p
     * @param name
     * @return int
     */
    private int getInt(Properties p, String name) {
        String value = p != null ? p.getProperty(name) : null;
        if (value == null) {
            return INT_DEFAULT_DEFAULT;
        }
        int ival = 0;
        try {
            ival = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public long getLong(String name) {
        return getLong(properties, name);
    }

    /**
     * Helper function: gets long for a given name.
     * @param p the properties
     * @param name the name of the property
     * 
     * @return the return value as a long
     */
    private long getLong(Properties p, String name) {
        String value = p != null ? p.getProperty(name) : null;
        if (value == null) {
            return LONG_DEFAULT_DEFAULT;
        }
        long ival = LONG_DEFAULT_DEFAULT;
        try {
            ival = Long.parseLong(value);
        } catch (NumberFormatException e) {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public String getString(String name) {
        return getString(properties, name);
    }

    /**
     * Helper function: gets string for a given name.
     * @param p the properties
     * @param name the property name
     * 
     * @return the return value as a String
     */
    private String getString(Properties p, String name) {
        String value = p != null ? p.getProperty(name) : null;
        if (value == null) {
            return STRING_DEFAULT_DEFAULT;
        }
        return value;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean isDefault(String name) {
        return (!properties.containsKey(name) && defaultProperties
                .containsKey(name));
    }

    /**
     * Prints the contents of this preference store to the given print stream.
     * 
     * @param out
     *            the print stream
     */
    public void list(PrintStream out) {
        properties.list(out);
    }

    /**
     * Prints the contents of this preference store to the given print writer.
     * 
     * @param out
     *            the print writer
     */
    public void list(PrintWriter out) {
        properties.list(out);
    }

    /**
     * Loads this preference store from the file established in the constructor
     * <code>PreferenceStore(java.lang.String)</code> (or by
     * <code>setFileName</code>). Default preference values are not affected.
     * 
     * @exception java.io.IOException
     *                if there is a problem loading this store
     */
    public void load() throws IOException {
        if (filename == null) {
            throw new IOException("File name not specified");//$NON-NLS-1$
        }
        FileInputStream in = new FileInputStream(filename);
        load(in);
        in.close();
    }

    /**
     * Loads this preference store from the given input stream. Default
     * preference values are not affected.
     * 
     * @param in
     *            the input stream
     * @exception java.io.IOException
     *                if there is a problem loading this store
     */
    public void load(InputStream in) throws IOException {
        properties.load(in);
        dirty = false;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean needsSaving() {
        return dirty;
    }

    /**
     * Returns an enumeration of all preferences known to this store which have
     * current values other than their default value.
     * 
     * @return an array of preference names
     */
    @SuppressWarnings("unchecked")
	public String[] preferenceNames() {
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<String> it = (Enumeration<String>) properties.propertyNames();
        while (it.hasMoreElements()) {
            list.add(it.nextElement());
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void putValue(String name, String value) {
        String oldValue = getString(name);
        if (oldValue == null || !oldValue.equals(value)) {
            setValue(properties, name, value);
            dirty = true;
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        removeListenerObject(listener);
    }

    /**
     * Saves the non-default-valued preferences known to this preference store
     * to the file from which they were originally loaded.
     * 
     * @exception java.io.IOException
     *                if there is a problem saving this store
     */
    public void save() throws IOException {
        if (filename == null) {
            throw new IOException("File name not specified");//$NON-NLS-1$
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            save(out, HEADER);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Saves this preference store to the given output stream. The given string
     * is inserted as header information.
     * 
     * @param out
     *            the output stream
     * @param header
     *            the header
     * @exception java.io.IOException
     *                if there is a problem saving this store
     */
    public void save(OutputStream out, String header) throws IOException {
        properties.store(out, header);
        dirty = false;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, double value) {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, float value) {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, int value) {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, long value) {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, String value) {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, boolean value) {
        setValue(defaultProperties, name, value);
    }

    /**
     * Sets the name of the file used when loading and storing this preference
     * store.
     * <p>
     * Afterward, the methods <code>load()</code> and <code>save()</code>
     * can be used to load and store this preference store.
     * </p>
     * 
     * @param name the file name
     * @see #load()
     * @see #save()
     */
    public void setFilename(String name) {
        filename = name;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setToDefault(String name) {
        Object oldValue = properties.get(name);
        properties.remove(name);
        dirty = true;
        Object newValue = null;
        if (defaultProperties != null) {
            newValue = defaultProperties.get(name);
        }
        firePropertyChangeEvent(name, oldValue, newValue);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, double value) {
        double oldValue = getDouble(name);
        if (oldValue != value) {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Double(oldValue), new Double(
                    value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, float value) {
        float oldValue = getFloat(name);
        if (oldValue != value) {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Float(oldValue), new Float(value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, int value) {
        int oldValue = getInt(name);
        if (oldValue != value) {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Integer(oldValue), new Integer(
                    value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, long value) {
        long oldValue = getLong(name);
        if (oldValue != value) {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Long(oldValue), new Long(value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, String value) {
        String oldValue = getString(name);
        if (oldValue == null || !oldValue.equals(value)) {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, boolean value) {
        boolean oldValue = getBoolean(name);
        if (oldValue != value) {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(
                    value));
        }
    }

    /**
     * Helper method: sets value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Properties p, String name, double value) {
        p.put(name, Double.toString(value));
    }

    /**
     * Helper method: sets value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Properties p, String name, float value) {
        p.put(name, Float.toString(value));
    }

    /**
     * Helper method: sets value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Properties p, String name, int value) {
        p.put(name, Integer.toString(value));
    }

    /**
     * Helper method: sets the value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Properties p, String name, long value) {
        p.put(name, Long.toString(value));
    }

    /**
     * Helper method: sets the value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Properties p, String name, String value) {
        p.put(name, value);
    }

    /**
     * Helper method: sets the value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Properties p, String name, boolean value) {
        p.put(name, value == true ? IPreferenceStore.TRUE
                : IPreferenceStore.FALSE);
    }
}
