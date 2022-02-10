package org.sonatype.cs.nexusiqgetmetrics;

import org.sonatype.cs.nexusiqgetmetrics.reports.*;
import org.sonatype.cs.nexusiqgetmetrics.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class NexusIQGetmetricsApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(NexusIQGetmetricsApplication.class);

	@Autowired
	private PolicyIdsService policyIdsService;

	@Autowired
	private NexusIQAPIPagingService nexusIQAPIPagingService;

	@Autowired
	private NexusIQApiService nexusIQApiService;

	@Autowired
	private FileIoService fileIoService;

	@Autowired
	private NexusIQSuccessMetrics nexusIQSuccessMetrics;

	@Value("${metrics.successmetrics}")
	private boolean successmetrics;

	@Value("${metrics.applicationsevaluations}")
	private boolean applicationsevaluations;

	@Value("${metrics.waivers}")
	private boolean waivers;

	@Value("${metrics.policyviolations}")
	private boolean policyviolations;

	@Value("${metrics.firewall}")
	private boolean firewall;


	public static void main(String[] args) {
		SpringApplication.run(NexusIQGetmetricsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Starting MgenApplication");

		fileIoService.initMetricsDir();

		if (successmetrics){
			nexusIQSuccessMetrics.createSuccessMetricsCsvFile();
		}

		if (applicationsevaluations){
			nexusIQApiService.makeReport(new ApplicationEvaluations(), "/reports/applications");
		}

		if (waivers){
			nexusIQApiService.makeReport(new Waivers(), "/reports/components/waivers");
		}

		if (policyviolations){
			nexusIQApiService.makeReport(new PolicyViolations(), policyIdsService.getPolicyIdsEndpoint());
		}

		if(firewall){
			nexusIQApiService.makeReport(new AutoReleasedFromQuarantineConfig(), "/firewall/releaseQuarantine/configuration");

			nexusIQApiService.makeReport(new AutoReleasedFromQuarantineSummary(), "/firewall/releaseQuarantine/summary");
			nexusIQApiService.makeReport(new QuarantinedComponentsSummary(), "/firewall/quarantine/summary");

			nexusIQAPIPagingService.makeReport(new QuarantinedComponents(), "/firewall/components/quarantined");
			nexusIQAPIPagingService.makeReport(new AutoReleasedFromQuarantineComponents(), "/firewall/components/autoReleasedFromQuarantine");
		}
	}

}
