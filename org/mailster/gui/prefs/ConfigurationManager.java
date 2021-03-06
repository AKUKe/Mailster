/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005-2006 Feed'n Read Development Team                        *
 * http://sourceforge.net/fnr                                                  *
 *                                                                             *
 * All rights reserved.                                                        *
 *                                                                             *
 * This program and the accompanying materials are made available under the    *
 * terms of the Common Public License v1.0 which accompanies this distribution,*
 * and is available at                                                         *
 * http://www.eclipse.org/legal/cpl-v10.html                                   *
 *                                                                             *
 * A copy is found in the file cpl-v10.html and important notices to the       *
 * license from the team is found in the textfile LICENSE.txt distributed      *
 * in this package.                                                            *
 *                                                                             *
 * This copyright notice MUST APPEAR in all copies of the file.                *
 *                                                                             *
 * Contributors:                                                               *
 *    Feed'n Read - initial API and implementation                             *
 *                  (smachhau@users.sourceforge.net)                           *
 *******************************************************************************/

package org.mailster.gui.prefs;

import java.io.File;

import org.mailster.gui.prefs.store.MailsterPrefStore;

/**
 * Manages and organizes the entire application configuration. The class itself
 * only holds the keys to access the configuration values from the internal data
 * structure in shape of the {@link #CONFIG_STORE}.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class ConfigurationManager
{

    /**
     * The application configuration filename
     */
    public final static String CONFIGURATION_FILENAME = "config.properties";

    /**
     * The ConfigurationStore used to administrate the persistent configuration
     * settings.
     */
    public final static MailsterPrefStore CONFIG_STORE = new MailsterPrefStore(
            CONFIGURATION_FILENAME);

    /* Application infos */

    /**
     * Application version number
     */
    public final static String MAILSTER_VERSION_NB	= "v1.0.0 beta";

    /**
     * Application version codename string
     */
    public final static String MAILSTER_VERSION_CODENAME = " - Codename Professor X";

    /**
     * Application version string
     */
    public final static String MAILSTER_VERSION = "Mailster "+MAILSTER_VERSION_NB;
    
    /**
     * The url to use when checking if version is up to date.
     */
    public final static String MAILSTER_VERSION_CHECK_URL = "http://tedorg.free.fr/downloads/mailster.ver";
    
    /**
     * Application homepage
     */
    public final static String MAILSTER_HOMEPAGE = "http://tedorg.free.fr/en/projects.php";
    
    /**
     * Application download page
     */
    public final static String MAILSTER_DOWNLOAD_PAGE = "https://sourceforge.net/project/showfiles.php?group_id=189956";
    
    /**
     * Application copyright
     */
    public final static String MAILSTER_COPYRIGHT = "Copyright (C) De Oliveira Edouard 2007-2013";

    /**
     * The url of the application forum
     */
    public final static String APPLICATION_FORUM_HOMEPAGE = "http://sourceforge.net/forum/forum.php?forum_id=667799";

    /**
     * The url of the application bug tracker
     */
    public final static String APPLICATION_BUGTRACKER_HOMEPAGE = "http://sourceforge.net/tracker/?group_id=189956&atid=931405";

    /**
     * The url of the application feature request tracker
     */
    public final static String APPLICATION_FEATURE_REQUEST_HOMEPAGE = "http://sourceforge.net/tracker/?group_id=189956&atid=931408";

    /**
     * The author email
     */
    public final static String AUTHOR_EMAIL = "doe_wanted@yahoo.fr";

    /* Window options */
    /**
     * The window x coordinate setting
     */
    public final static String WINDOW_X_KEY = "window.x";

    /**
     * The window y coordinate setting
     */
    public final static String WINDOW_Y_KEY = "window.y";

    /**
     * The window width setting
     */
    public final static String WINDOW_WIDTH_KEY = "window.width";

    /**
     * The window height setting
     */
    public final static String WINDOW_HEIGHT_KEY = "window.height";

    /* Control divider options */
    /**
     * The key to access the output view divider ratio
     */
    public final static String FILTER_VIEW_RATIO_KEY = "window.filterView.divider.ratio";

    /**
     * The key to access the main view divider ratio
     */
    public final static String TABLE_VIEW_RATIO_KEY = "window.tableView.divider.ratio";

    /* General options */
    /**
     * The key to access the general options
     */
    public final static String GENERAL_OPTIONS_KEY = "generalOptions";

    /**
     * The key to access the state of the mail panel
     */
    public final static String MAIL_PANEL_MINIMIZED_KEY = "window.mail.pshelfpanel.minimized";
    
    /**
     * The flag to decide whether to ask if a Mail should be really deleted by
     * the help of an user dialog.
     */
    public final static String ASK_ON_REMOVE_MAIL_KEY = "general.ask.OnRemoveMail";

    /**
     * The flag to decide whether to restore main window size, location
     * and dividers positions at startup.
     */
    public final static String APPLY_MAIN_WINDOW_PARAMS_KEY = "general.apply.window.params.onStartup";

    /**
     * The preferred browser.
     */
    public final static String PREFERRED_BROWSER_KEY = "general.preferred.browser";

    /**
     * The preferred content type for email viewing.
     */
    public final static String PREFERRED_CONTENT_TYPE_KEY = "general.preferred.contentType";

    /**
     * The default mail queue refresh interval.
     */
    public final static String MAIL_QUEUE_REFRESH_INTERVAL_KEY = "general.mailQueue.refresh.interval";

    /* Language options */
    /**
     * The key to access the language options
     */
    public final static String LANGUAGE_OPTIONS_KEY = "languageOptions";

    /**
     * The language setting as iso2 code which determines the language of the
     * user interface.
     */
    public final static String LANGUAGE_KEY = "ui.language";

    /* Connection options */
    /**
     * Key to access the protocols options
     */
    public final static String PROTOCOLS_OPTIONS_KEY = "protocolsOptions";

    /**
     * Key to access the protocols options
     */
    public final static String START_SMTP_ON_STARTUP_KEY = "protocols.start.smtp.onStartUp";
    
    /**
     * <code>true</code> if POP3 service should start on SMTP start up
     */
    public final static String START_POP3_ON_SMTP_START_KEY = "protocols.start.pop3.onSMTPStart";
    
    /**
     * <code>true</code> if SSL clients authentication is required
     */
    public final static String AUTH_SSL_CLIENT_KEY = "protocols.ssl.client.requireAuth";
    
    /**
     * The SSL protocol in use
     */
    public final static String PREFERRED_SSL_PROTOCOL_KEY = "protocols.ssl.server.protocol";
    
    /**
     * The crypto strength of the automatically generated certificates.
     */
    public final static String CRYPTO_STRENGTH_KEY =  "protocols.crypto.strength";
    
    /* Pop3 options */
    /**
     * The key to access the pop3 options
     */
    public final static String POP3_OPTIONS_KEY = "pop3Options";
    
    /**
     * The POP3 server host/address
     */
    public final static String POP3_SERVER_KEY = "pop3.address";
    
    /**
     * The POP3 server port
     */
    public final static String POP3_PORT_KEY = "pop3.port";
    
    /**
     * The POP3 special account name
     */
    public final static String POP3_SPECIAL_ACCOUNT_KEY = "pop3.specialAccount";
    
    /**
     * The POP3 mailboxes password
     */
    public final static String POP3_PASSWORD_KEY = "pop3.password";
    
    /**
     * The POP3 maximum of parallel connections
     */
    public final static String POP3_MAX_CONNECTIONS_KEY = "pop3.maximum.connections";

    /**
     * Specifies the timeout (in milliseconds) to establish the connection to
     * the host for the POP3 server.
     */
    public final static String POP3_CONNECTION_TIMEOUT_KEY = "pop3.connection.timeout";

    /**
     * <code>true</code> if only secured authentication mechanisms are allowed.
     */
    public final static String POP3_REQUIRE_SECURE_AUTH_METHOD_KEY = "pop3.authentication.secureMethodRequired";

    /**
     * <code>true</code> if APOP authentication mechanism is allowed.
     */
    public final static String POP3_ALLOW_APOP_AUTH_METHOD_KEY = "pop3.authentication.allowAPOP";
    
    /* SMTP options */
    /**
     * The key to access the smtp options
     */
    public final static String SMTP_OPTIONS_KEY = "smtpOptions";
    
    /**
     * The SMTP server host/address
     */
    public final static String SMTP_SERVER_KEY = "smtp.address";
    
    /**
     * The SMTP server port
     */
    public final static String SMTP_PORT_KEY = "smtp.port";
    
    /**
     * The SMTP maximum of parallel connections
     */
    public final static String SMTP_MAX_CONNECTIONS_KEY = "smtp.maximum.connections";

    /**
     * Specifies the timeout (in milliseconds) to establish the connection to
     * the host for the SMTP server.
     */
    public final static String SMTP_CONNECTION_TIMEOUT_KEY = "smtp.connection.timeout";

    /* Enclosure options */
    /**
     * Key to access the attachments options
     */
    public final static String ENCLOSURES_OPTIONS_KEY = "enclosuresOptions";

    /**
     * The default directory to store enclosures to
     */
    public final static String DEFAULT_ENCLOSURES_DIRECTORY_KEY = "enclosures.default.directory";

    /**
     * <code>true</code> to automatically execute an enclosure in its
     * associated application. <code>false</code> for quiet mode (no automatic
     * execution).
     */
    public final static String EXECUTE_ENCLOSURE_ON_CLICK_KEY = "enclosures.execute.on.click";

    /* Tray options */
    /**
     * Key to access the tray options
     */
    public final static String TRAY_OPTIONS_KEY = "trayOptions";

    /**
     * <code>true</code> to send to system tray if the
     * <code>MainApplicationWindow</code> is minimized.
     */
    public final static String SEND_TO_TRAY_ON_MINIMIZE_KEY = "tray.sendToTray.OnMinimize";

    /**
     * <code>true</code> to send to system tray if the
     * <code>MainApplicationWindow</code> is closed.
     */
    public final static String SEND_TO_TRAY_ON_CLOSE_KEY = "tray.sendToTray.OnClose";

    /**
     * <code>true</code> to send to tray if the application starts up.
     */
    public final static String SEND_TO_TRAY_ON_SERVER_START_KEY = "tray.sendToTray.OnServerStart";

    /**
     * <code>true</code> to automatically hide tray notifications.
     */
    public final static String AUTO_HIDE_NOTIFICATIONS_KEY = "tray.notifications.auto.hide";

    /**
     * <code>true</code> to notify when new messages were received.
     */
    public final static String NOTIFY_ON_NEW_MESSAGES_RECEIVED_KEY = "tray.notifications.notify.OnNewMessages.received";

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ConfigurationManager()
    {
    }

    /**
     * Initializes the <code>ConfigurationManager</code>.
     */
    static
    {
    	verifyDirectorySetting(DEFAULT_ENCLOSURES_DIRECTORY_KEY);
    }

    /**
     * Verifies if the directory denoted by the given <code>key</code> exists.
     * If not it is created.
     * 
     * @param key the key to access the directory setting
     */
    private static void verifyDirectorySetting(String key)
    {
        File dir = new File(CONFIG_STORE.getString(key));
        if (!dir.exists())
            dir.mkdir();
    }
}
