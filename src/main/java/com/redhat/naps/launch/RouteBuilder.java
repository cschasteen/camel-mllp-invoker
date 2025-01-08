package com.redhat.naps.launch;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.SimpleDateFormat;
import java.util.Date;
import static org.apache.camel.component.hl7.HL7.hl7terser;

@ApplicationScoped
public class RouteBuilder extends org.apache.camel.builder.RouteBuilder {

    static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    static String hl7MessageTemplate
            // = "MSH|^~\\&|REQUESTING|ICE|INHOUSE|RTH00|<MESSAGE_TIMESTAMP>||ORM^O01|<MESSAGE_CONTROL_ID>|D|2.3|||AL|NE|||" + '\r'
            // + "PID|1||ICE999999^^^ICE^ICE||Testpatient^Testy^^^Mr||19740401|M|||123 Barrel Drive^^^^SW18 4RT|||||2||||||||||||||"
            // + '\r'
            // + "NTE|1||Free text for entering clinical details|" + '\r'
            // + "PV1|1||^^^^^^^^<LOCATION_VAR>|||||||||||||||NHS|" + '\r'
            // + "ORC|NW|213||175|REQ||||20080808093202|ahsl^^Administrator||G999999^TestDoctor^GPtests^^^^^^NAT|^^^^^^^^Admin Location | 819600|200808080932||RTH00||ahsl^^Administrator||"
            // + '\r'
            // + "OBR|1|213||CCOR^Serum Cortisol ^ JRH06|||200808080932||0.100||||||^|G999999^TestDoctor^GPtests^^^^^^NAT|819600|ADM162||||||820|||^^^^^R||||||||"
            // + '\r'
            // + "OBR|2|213||GCU^Serum Copper ^ JRH06 |||200808080932||0.100||||||^|G999999^TestDoctor^GPtests^^^^^^NAT|819600|ADM162||||||820|||^^^^^R||||||||"
            // + '\r'
            // + "OBR|3|213||THYG^Serum Thyroglobulin ^JRH06|||200808080932||0.100||||||^|G999999^TestDoctor^GPtests^^^^^^NAT|819600|ADM162||||||820|||^^^^^R||||||||"
            // + '\r'
            // + '\n';


            = "MSH^~|\\&^VAFC PIMS^589~KANSAS-CITY.MED.VA.GOV~DNS^R2-VITALS^200HD~HDR.MED.VA.GOV^<MESSAGE_TIMESTAMP>^^ORU~R01^<MESSAGE_CONTROL_ID>^P^2.4^^^AL^NE^USA" + '\n'
            +"PID^1^123456789V987654321^123456789V987654321~~~USVHA&&0363~NI~VA FACILITY ID&200M&L|987987987~~~USSSA&&0363~SS~<LOCATION_VAR>|\"\"~~~USDOD&&0363~TIN~VA FACILITY ID&589&L|\"\"~~~USDOD&&0363~FIN~VA FACILITY ID&589&L|\"\"~~~USIRS&&0363~NI~VA FACILITY ID&589&L|735944~~~USVHA&&0363~PI~VA FACILITY ID&589&L^18366402~~~USVHA&&0363~PI~VA FACILITY ID&742V1&L^Just~A~Name~~~~L|Another~~~~~~N^Name~~~~~~M^19770101^M^^2106-3-SLF~~0005~2106-3~~CDC^An address~\"\"~Somewhere~KS~66542~USA~P~\"\"~177|~~TOPEKA~KS~~\"\"~N|An Address~\"\"~Somewhere Else~KS~66542~USA~R~\"\"~177^177^(111)333-4444~PRN~PH|(111)333-4444~WPN~PH|(111)222-3333~ORN~CP|~NET~INTERNET~email.address@email.com^(111)222-3333^^M^0^^123456^^^2135-2-SLF~~0189~2135-2~~CDC^Somewhere KS^N^^^^^\"\"^^" + '\n'
            +"ORC^RE^^123456789~589_120.5^^^^^^^^^^$ORTHPA~38917~~~~~~~TO-ORTHO PA^^^^589A5~EASTERN KS HCS TOPEKA DIV~L^^^^KANSAS CITY VAMC" + '\n'
            +"OBR^^^123456789~589_120.5^987654312~TEMPERATURE~99VA120.51^^^20241009092435-0500^20241009092452-0500^^^^^^^^^^^^^^20241009092452-0500^^^F^^^^^^^^^773541~Somebody~Somebody~G~~~~VistA200" + '\n'
            +"OBX^^ST^987654312~TEMPERATURE~99VA120.51^^97.3^F~F~L^^^^^F^^^^^773541~Somebody~Somebody~G~~~~VistA200";

    public static String getHL7Message() {
        
        String tmpMessage = hl7MessageTemplate.replaceFirst("<MESSAGE_TIMESTAMP>", timestampFormat.format(new Date()));
        String tmpMessage2 = tmpMessage.replaceFirst("<LOCATION_VAR>","{{mllp.patlocation}}");
        return tmpMessage2.replaceFirst("<MESSAGE_CONTROL_ID>", String.format("%05d", 1));
    }

    @Override
    public void configure() throws Exception {
        from("timer:send-mllp?delay=-1&repeatCount=1")
                .routeId("FromTimer2MLLP")
                .setBody(simple(getHL7Message()))
                .to("log:before?showAll=true&multiline=true")
                .to("mllp://{{mllp.ip}}:{{mllp.port}}")
                .log("Message sent via MLLP to {{mllp.ip}}:{{mllp.port}}")
                .log("Received Type - ${header.CamelMllpAcknowledgementType}")
                .log("Received - ${header.CamelMllpAcknowledgementString}")
                .to("log:after?showAll=true&multiline=true");
    }
}
