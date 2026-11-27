package no.nav.foreldrepenger.soknad.vedlegg.sjekkere.virusscan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class ScanResult {
    private final String filename;
    private final Result result;
    private final String virus;
    private final String error;

    @JsonCreator
    public ScanResult(@JsonProperty("filename") String filename,
                      @JsonProperty("result") Result result,
                      @JsonProperty("virus") String virus,
                      @JsonProperty("error") String error) {
        this.filename = filename;
        this.result = result;
        this.virus = virus;
        this.error = error;
    }

    public String getFilename() {
        return filename;
    }

    public Result getResult() {
        return result;
    }

    public String getVirus() {
        return virus;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [filename=" + filename + ", result=" + result + ", virus=" + virus + ", error=" + error + "]";
    }
}
