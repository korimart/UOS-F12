package com.korimart.f12;

import java.util.concurrent.CompletableFuture;

public class CourseListFetchParser extends AsyncFetchParser {
    private AsyncFetchParser personalInfoFetchParser;
    private AsyncFetchParser schoolListFetchParser;

    private boolean refetchPersonalInfo;
    private boolean refetchSchoolList;
    private boolean fetchMine;

    public CourseListFetchParser(AsyncFetchParser personalInfoFetchParser,
                                 AsyncFetchParser schoolListFetchParser) {
        super(URLStorage.getCourseListUrl(), null, CourseListParser.INSTANCE);
        this.personalInfoFetchParser = personalInfoFetchParser;
        this.schoolListFetchParser = schoolListFetchParser;
    }

    @Override
    public CompletableFuture<Void> fetch(boolean refetch) {
        return CompletableFuture.allOf(
                personalInfoFetchParser.fetchAndParse(refetchPersonalInfo),
                schoolListFetchParser.fetchAndParse(refetchSchoolList)
        ).thenCompose(ignored -> {
            if (fetchMine){
                SchoolListParser.Result schoolList =
                        (SchoolListParser.Result) schoolListFetchParser.getpCache().data;
                PersonalInfoParser.Result persInfo =
                        (PersonalInfoParser.Result) personalInfoFetchParser.getpCache().data;

                setParams(URLStorage.getCourseListParam(
                        schoolList.latestSchoolYear,
                        schoolList.latestSemester,
                        persInfo.schoolCode,
                        persInfo.deptCode
                ));
            }

            return super.fetch(refetch);
        });
    }

    public void setRefetchPersonalInfo(boolean refetchPersonalInfo) {
        this.refetchPersonalInfo = refetchPersonalInfo;
    }

    public void setRefetchSchoolList(boolean refetchSchoolList) {
        this.refetchSchoolList = refetchSchoolList;
    }

    public void setFetchMine(boolean fetchMine) {
        this.fetchMine = fetchMine;
    }
}
