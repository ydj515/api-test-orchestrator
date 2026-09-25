package com.example.reportserver;

import com.example.reportserver.config.CliConfig;
import com.example.reportserver.presentation.run.cli.CatsPublishCli;
import com.example.reportserver.presentation.run.cli.KaratePublishCli;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReportServerApplication {

    public static void main(String[] args) {
        if (args.length > 0) {
            String command = args[0];
            String[] rest = Arrays.copyOfRange(args, 1, args.length);
            if (KaratePublishCli.COMMAND.equals(command)) {
                KaratePublishCli.execute(rest, CliConfig::services);
                return;
            }
            if (CatsPublishCli.COMMAND.equals(command)) {
                CatsPublishCli.execute(rest, CliConfig::services);
                return;
            }
        }
        SpringApplication.run(ReportServerApplication.class, args);
    }
}
