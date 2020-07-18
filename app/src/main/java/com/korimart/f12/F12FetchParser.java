package com.korimart.f12;

import java.util.concurrent.CompletableFuture;

public class F12FetchParser extends AsyncFetchParser {
    private AsyncFetchParser f12InfoFetchParser;

    public F12FetchParser(AsyncFetchParser f12InfoFetchParser) {
        super(URLStorage.getF12URL(), null, new F12Parser());
        this.f12InfoFetchParser = f12InfoFetchParser;
    }

    public void setNoPnp(boolean noPnp){
        ((F12Parser) getWiseParser()).setNoPnp(noPnp);
    }

    @Override
    public CompletableFuture<Void> fetch(boolean refetch){
        return f12InfoFetchParser.fetchAndParse(refetch)
                .thenCompose(ignored -> {
                    F12InfoParser.Result parsed = (F12InfoParser.Result) f12InfoFetchParser.getpCache().data;
                    setParams(URLStorage.getF12Params(parsed.schoolYear, parsed.semester));
                    return super.fetch(refetch);
                });
    }
}
