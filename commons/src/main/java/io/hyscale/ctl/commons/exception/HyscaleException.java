package io.hyscale.ctl.commons.exception;

public class HyscaleException extends Exception {

    private HyscaleErrorCode hyscaleErrorCode;
    private String[] args;

    public HyscaleException(HyscaleErrorCode hyscaleErrorCode, String... args) {
        super(hyscaleErrorCode.getErrorMessage());
        this.hyscaleErrorCode = hyscaleErrorCode;
        this.args = args;
    }

    public HyscaleException(HyscaleErrorCode hyscaleErrorCode) {
        super(hyscaleErrorCode.getErrorMessage());
        this.hyscaleErrorCode = hyscaleErrorCode;
    }

    public HyscaleException(Throwable throwable, HyscaleErrorCode hyscaleErrorCode) {
        super(hyscaleErrorCode.getErrorMessage(), throwable);
        this.hyscaleErrorCode = hyscaleErrorCode;
    }

    public HyscaleException(Throwable throwable, HyscaleErrorCode hyscaleErrorCode, String... args) {
        super(hyscaleErrorCode.getErrorMessage(), throwable);
        this.hyscaleErrorCode = hyscaleErrorCode;
        this.args = args;
    }

    public HyscaleErrorCode getHyscaleErrorCode() {
        return hyscaleErrorCode;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (args != null) {
            sb.append(args.length != 0 ? String.format(hyscaleErrorCode.getErrorMessage().replaceAll("\\{\\}", "%s"), args)
                    : hyscaleErrorCode.getErrorMessage());
        } else {
            sb.append(hyscaleErrorCode.getErrorMessage());
        }
        sb.append("]");
        return sb.toString();
    }


    @Override
    public String getMessage() {
        return toString();
    }
}
