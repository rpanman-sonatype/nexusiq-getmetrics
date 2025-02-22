package org.sonatype.cs.getmetrics.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.cs.getmetrics.service.CsvFileService;
import org.sonatype.cs.getmetrics.service.FileIoService;
import org.sonatype.cs.getmetrics.util.FilenameInfo;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.util.ArrayList;
import java.util.List;

public class Waivers implements CsvFileService  {
    private static final Logger log = LoggerFactory.getLogger(Waivers.class);

    @Override
    public void makeCsvFile(FileIoService f, JsonReader reader) {
        log.info("Making Waivers report");

        List<String[]> data = new ArrayList<>();
        data.add(FilenameInfo.waiversFileHeader);

        JsonObject obj = reader.readObject();

        JsonArray applicationWaivers = obj.getJsonArray("applicationWaivers");
        List<String[]> aw = this.doWaivers("application", applicationWaivers);

        JsonArray repositoryWaivers = obj.getJsonArray("repositoryWaivers");
        List<String[]> rw = this.doWaivers("repository", repositoryWaivers);

        data.addAll(aw);
        data.addAll(rw);

        f.writeCsvFile(FilenameInfo.waiversCsvFile, data);
    }

    @Override
    public void makeCsvFile(FileIoService f, JsonObject reader) {

    }

    public List<String[]> doWaivers(String waiverType, JsonArray waivers){

        List<String[]> data = new ArrayList<>();

        for (JsonObject result : waivers.getValuesAs(JsonObject.class)) {

            JsonObject waiverObj = result.getJsonObject(waiverType);
            String applicationName = waiverObj.getString("publicId");

            JsonArray stages = result.getJsonArray("stages");

            for (JsonObject stage : stages.getValuesAs(JsonObject.class)){
                String stageId = stage.getString("stageId");

                JsonArray componentViolations = stage.getJsonArray("componentPolicyViolations");

                for (JsonObject componentViolation : componentViolations.getValuesAs(JsonObject.class)){
                    JsonObject component = componentViolation.getJsonObject("component");
                    String packageUrl = component.getString("packageUrl");

                    JsonArray waivedPolicyViolations = componentViolation.getJsonArray("waivedPolicyViolations");

                    for (JsonObject waivedPolicyViolation : waivedPolicyViolations.getValuesAs(JsonObject.class)){
                        String policyName = waivedPolicyViolation.getString("policyName");
                        int threatLevel = waivedPolicyViolation.getInt("threatLevel");

                        JsonObject policyWaiver = waivedPolicyViolation.getJsonObject("policyWaiver");

                        String comment = (policyWaiver.get("comment") != null) ? String.valueOf(policyWaiver.get("comment")) : "";
                        String createTime = (policyWaiver.get("createTime") != null) ? String.valueOf(policyWaiver.get("createTime")) : "";
                        String expiryTime = (policyWaiver.get("expiryTime") != null) ? String.valueOf(policyWaiver.get("expiryTime")) : "";

                        String[] line = {applicationName, stageId, packageUrl, policyName, String.valueOf(threatLevel), comment, createTime, expiryTime};
                        data.add(line);
                    }
                }
            }
        }

        return data;
    }
}
