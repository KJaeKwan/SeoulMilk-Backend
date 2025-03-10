package Seoul_Milk.sm_server.domain.taxInvoiceValidation.enums;

public enum ThreadTerm {
    THREAD_TERM(1000L);

    private final long terms;

    ThreadTerm(long terms) {
        this.terms = terms;
    }

    public long getMillis(){
        return this.terms;
    }
}
