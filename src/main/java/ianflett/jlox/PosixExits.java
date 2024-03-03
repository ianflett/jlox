package ianflett.jlox;

/**
 * Defines standard system exit codes as defined by POSIX systems.
 *
 * @see <a
 *     href="https://man.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html">sysexits.h</a>
 */
public enum PosixExits {

    /** Program executed successfully. */
    OK(0),

    /**
     * Command used incorrectly, e.g., wrong number of arguments, bad flag, bad syntax in parameter,
     * or whatever.
     */
    USAGE(64),

    /**
     * Input data incorrect in some way. Should only be used for user's data and not system files.
     */
    DATAERR(65),

    /**
     * Input file (not system file) did not exist or not readable. Could also include errors like
     * "No message" to mailer (if it cared to catch it).
     */
    NOINPUT(66),

    /** User specified did not exist. Might be used for mail addresses or remote logins. */
    NOUSER(67),

    /** Host specified did not exist. Used in mail addresses or network requests. */
    NOHOST(68),

    /**
     * Service unavailable. Can occur if support program or file does not exist. Can also be used as
     * catchall message when something you wanted to do doesn't work, but you don't know why.
     */
    UNAVAILABLE(69),

    /**
     * Internal software error detected. Should be limited to non-operating system related errors as
     * possible.
     */
    SOFTWARE(70),

    /**
     * Operating system error detected. Intended to be used for such things as "cannot fork",
     * "cannot create pipe", or similar. Includes things like getuid returning user that does not
     * exist in passwd file.
     */
    OSERR(71),

    /**
     * Some system file (e.g., /etc/passwd, /var/run/utmp, etc.) does not exist, cannot be opened,
     * or has some sort of error (e.g., syntax error).
     */
    OSFILE(72),

    /** (User specified) output file cannot be created. */
    CANTCREAT(73),

    /** Error occurred while doing I/O on some file. */
    IOERR(74),

    /**
     * Temporary failure, indicating something that is not really an error. In sendmail, this means
     * mailer (e.g.) could not create connection, and request should be reattempted later.
     */
    TEMPFAIL(75),

    /** Remote system returned something "not possible" during a protocol exchange. */
    PROTOCOL(76),

    /**
     * Insufficient permission to perform operation. Not intended for file system problems, which
     * should use {@link #NOINPUT} or {@link #CANTCREAT}, but rather for higher level permissions.
     */
    NOPERM(77),

    /** Something found in unconfigured or misconfigured state. */
    CONFIG(78);

    /** Stores associated exit code. */
    private final int code;

    /**
     * Defines POSIX system exit code.
     *
     * @param code Associated exit code.
     */
    PosixExits(int code) {
        this.code = code;
    }

    /**
     * Gets associated exit code.
     *
     * @return Exit code.
     */
    public int getCode() {
        return code;
    }
}
