package com.terrafinosolutions.ac.graphql;

import io.terrafino.api.ac.ado.Ado;
import io.terrafino.api.ac.attribute.Attributes;
import io.terrafino.api.ac.timeseries.TsRecord;
import io.terrafino.api.ac.value.Values;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MockedAcAccess {
    public List<Ado> findAdosByBaseCurrency(String ccy) {
        switch (ccy) {
            case "EUR":
                return Arrays.asList(
                        createAdo("C0.FXS.100105", "FX SPOT EUR GBP", "EUR", "GBP"),
                        createAdo("C0.FXS.100106", "FX SPOT EUR USD", "EUR", "USD")
                );
            case "GBP":
                return Arrays.asList(
                        createAdo("C0.FXS.100101", "FX SPOT GBP EUR", "GBP", "EUR"),
                        createAdo("C0.FXS.100102", "FX SPOT GBP USD", "GBP", "USD")
                );
            case "USD":
                return Arrays.asList(
                        createAdo("C0.FXS.100103", "FX SPOT USD GBP", "USD", "GBP"),
                        createAdo("C0.FXS.100104", "FX SPOT USD EUR", "USD", "EUR")
                );
            default:
                return Collections.emptyList();
        }
    }

    private Ado createAdo(String id, String longname, String baseCurrency, String quoteCurrency) {
        Ado ado = new Ado(id, longname, "", null);
        ado.set("C0#SA010", baseCurrency);
        ado.set("C0#SA011", quoteCurrency);
        return ado;
    }

    public List<TsRecord> getTimeseriesFor(String adoId, String tree) {
        return readFile("/ts.amdpr")
                .stream()
                .filter(line -> line.startsWith(String.format("%s,%s,", tree, adoId)))
                .map(line -> {
                    String[] parts = line.split(",");
                    int date = Integer.valueOf(parts[4]);
                    int time = Integer.valueOf(parts[5]);
                    double close = Double.valueOf(parts[7]);
                    return new TsRecord(date, time, new Attributes("CLOSE"), new Values(close));

                })
                .sorted(Comparator.comparingInt(TsRecord::getDate))
                .collect(Collectors.toList());
    }

    public List<String> readFile(String filename) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        MockedAcAccess.class.getResourceAsStream(filename)
                ))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            // ignored
        }
        return lines;
    }
}
