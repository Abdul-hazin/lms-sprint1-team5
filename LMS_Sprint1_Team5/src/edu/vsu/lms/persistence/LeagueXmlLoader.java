package edu.vsu.lms.persistence;

import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;
import edu.vsu.lms.model.Player;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Loads a League from an XML file with structure:
 *
 * <league name="NBA">
 *   <teams>
 *     <team name="Boston Celtics">
 *       <players>
 *         <player>
 *           <firstName>Jaylen</firstName>
 *           <lastName>Brown</lastName>
 *           <position>G/F</position>
 *           <number>7</number>
 *         </player>
 *       </players>
 *     </team>
 *   </teams>
 * </league>
 */
public class LeagueXmlLoader {

    /**
     * Loads the League from a file.
     */
    public League loadLeagueFromFile(File xmlFile) throws Exception {
        // Build DOM
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        // Root <league>
        Element leagueElement = doc.getDocumentElement();
        if (!"league".equals(leagueElement.getTagName())) {
            throw new IllegalArgumentException("Root element must be <league>");
        }

        String leagueName = leagueElement.getAttribute("name");
        if (leagueName == null || leagueName.isBlank()) {
            leagueName = "Unnamed League";
        }

        League league = new League(leagueName);

        // <teams>
        NodeList teamsList = leagueElement.getElementsByTagName("teams");
        if (teamsList.getLength() == 0) {
            return league; // empty league
        }

        Element teamsElement = (Element) teamsList.item(0);
        NodeList teamNodes = teamsElement.getElementsByTagName("team");

        for (int i = 0; i < teamNodes.getLength(); i++) {
            Node tNode = teamNodes.item(i);
            if (tNode.getNodeType() != Node.ELEMENT_NODE) continue;

            Element teamElement = (Element) tNode;
            Team team = parseTeam(teamElement);

            // League stores teams by name
            league.getTeams().put(team.getName(), team);
        }

        return league;
    }

    /**
     * Parse a <team> element into a Team object.
     */
    private Team parseTeam(Element teamElement) {
        String teamName = teamElement.getAttribute("name");
        if (teamName == null || teamName.isBlank()) {
            teamName = "Unnamed Team";
        }

        Team team = new Team(teamName);

        NodeList playersList = teamElement.getElementsByTagName("players");
        if (playersList.getLength() == 0) {
            return team; // no players
        }

        Element playersElement = (Element) playersList.item(0);
        NodeList playerNodes = playersElement.getElementsByTagName("player");

        for (int i = 0; i < playerNodes.getLength(); i++) {
            Node pNode = playerNodes.item(i);
            if (pNode.getNodeType() != Node.ELEMENT_NODE) continue;

            Element playerElement = (Element) pNode;
            Player p = parsePlayer(playerElement);

            // Uses Team.addPlayer() correctly
            boolean added = team.addPlayer(p);
            if (!added) {
                System.err.println("⚠ Duplicate jersey number " + p.getNumber()
                        + " on team " + teamName + ". Player skipped.");
            }
        }

        return team;
    }

    /**
     * Parse a <player> element into a Player object.
     */
    private Player parsePlayer(Element playerElement) {
        String firstName = getChildText(playerElement, "firstName");
        String lastName  = getChildText(playerElement, "lastName");
        String position  = getChildText(playerElement, "position");
        String numText   = getChildText(playerElement, "number");

        int number = 0;
        if (numText != null && !numText.isBlank()) {
            try {
                number = Integer.parseInt(numText.trim());
            } catch (NumberFormatException ex) {
                System.err.println("⚠ Invalid jersey number: " + numText + " (using 0)");
            }
        }

        return new Player(firstName, lastName, number, position);
    }

    /**
     * Helper: safely get text from child element.
     */
    private String getChildText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;
        Node node = list.item(0);
        return node.getTextContent().trim();
    }
}
