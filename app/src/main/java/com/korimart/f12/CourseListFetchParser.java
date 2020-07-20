package com.korimart.f12;

import java.util.concurrent.CompletableFuture;

public class CourseListFetchParser extends AsyncFetchParser {
    private AsyncFetchParser f12InfoFetchParser;
    private AsyncFetchParser schoolListFetchParser;

    private boolean refetchF12Info;
    private boolean refetchSchoolList;
    private boolean fetchMine;

    public CourseListFetchParser(AsyncFetchParser f12InfoFetchParser,
                                 AsyncFetchParser schoolListFetchParser) {
        super(URLStorage.getCourseListUrl(), null, CourseListParser.INSTANCE);
        this.f12InfoFetchParser = f12InfoFetchParser;
        this.schoolListFetchParser = schoolListFetchParser;
    }

    @Override
    public CompletableFuture<Void> fetch(boolean refetch) {
        if (fetchMine){
            return CompletableFuture.allOf(
                    f12InfoFetchParser.fetchAndParse(refetchF12Info),
                    schoolListFetchParser.fetchAndParse(refetchSchoolList)
            ).thenCompose(ignored -> {
                SchoolListParser.Result schoolList =
                        (SchoolListParser.Result) schoolListFetchParser.getpCache().data;
                F12InfoParser.Result f12Info =
                        (F12InfoParser.Result) f12InfoFetchParser.getpCache().data;

                setParams(URLStorage.getCourseListParam(
                        schoolList.latestSchoolYear,
                        schoolList.latestSemester,
                        f12Info.schoolCode,
                        f12Info.deptCode
                ));

                return super.fetch(refetch);
            });
        }

        return super.fetch(refetch);
    }

    public void setRefetchF12Info(boolean refetchF12Info) {
        this.refetchF12Info = refetchF12Info;
    }

    public void setRefetchSchoolList(boolean refetchSchoolList) {
        this.refetchSchoolList = refetchSchoolList;
    }

    public void setFetchMine(boolean fetchMine) {
        this.fetchMine = fetchMine;
    }
}
