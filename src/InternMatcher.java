import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class InternMatcher {
	public static void main(String[] args)
	{
		final JFileChooser fc = new JFileChooser();
		final JFrame frame = new JFrame("JFileChooser Demo");
		System.out.println("Please select your .csv input file");
		int openFileSuccess = fc.showOpenDialog(frame);
		if(openFileSuccess != 0)
		{
			System.out.println("Could not open file. Exiting.");
			return;
		}
		File inputFile = fc.getSelectedFile();
		System.out.println("Parsing:" + inputFile.getName());
		ArrayList<Intern> interns = new ArrayList<Intern>();
		try {
			//Parse input file
			Scanner input = new Scanner(inputFile);
			input.useDelimiter("\n");
			input.next();//clear the headers

			while(input.hasNext())
			{
				String[] intern = input.next().split(",");
				Intern i = new Intern(intern[0],intern[1],intern[2],intern[3],intern[4]);
				interns.add(i);
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ArrayList<Intern> year1 = new ArrayList<Intern>();
		ArrayList<Intern> year2 = new ArrayList<Intern>();
		ArrayList<Intern> year3 = new ArrayList<Intern>();
		for(Intern i : interns)
		{
			if(i.year==1)
			{
				year1.add(i);
			}
			else if(i.year==2)
			{
				year2.add(i);
			}
			else if(i.year==3)
			{
				year3.add(i);
			}
			else
			{
				System.out.println("Please check input file, intern not years 1-3 found");
				return;
			}
		}
		
		Intern[][] teams = null;
		Intern[][] bestGuess = teams;
		int tries = 0;
		int maxTries = 981;
		while((teams==null || !teamsValid(teams)) && tries<maxTries)
		{
			tries++;
			teams = goodGuess(year1,year2,year3);
			if(teams==null)
			{
				continue;
			}
			if(bestGuess == null || teams.length > bestGuess.length)
			{
				bestGuess = teams;
			}
		}
		if(tries == maxTries)
		{
			teams=bestGuess;
		}
		
		
		
		PrintWriter csvWriter;
        File csvFile = new File("output.csv");

        // create file, disregard overwriting
        try {
            csvFile.createNewFile();
        } catch (IOException e) {
            System.err.println("CSV file not created");
        }
        
        try {
            csvWriter = new PrintWriter(csvFile);
            csvWriter.println("team_number, last_name, first_name, year, start_date, email");
            for(int i=0; i<teams.length; i++)
    		{
//            	csvWriter.println("Team "+i);
    			for(int j=0; j<teams[i].length; j++)
    			{
    				Intern intern = teams[i][j];
    				csvWriter.println(i+","+intern.lName+","+intern.fName+","+intern.year+","+intern.getDayString()+","+intern.email);
    				interns.remove(teams[i][j]);
    			}
    		}
            for(Intern intern:interns)
            {
            	csvWriter.println("N/A"+","+intern.lName+","+intern.fName+","+intern.year+","+intern.getDayString()+","+intern.email);
            }
            System.out.println();
            System.out.println("Team data successfully written to "+"output.csv");
            System.out.println();

            // close writer
            csvWriter.flush();
            csvWriter.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("CSV file not found");
        }
		//----------------------------------------------------------------------------
        
	}
	
	public static Intern[][] goodGuess(ArrayList<Intern> year1, ArrayList<Intern> year2, ArrayList<Intern> year3)
	{
//		System.out.println("eh");
//		ArrayList<Intern> y1 = new ArrayList<Intern>();
//		ArrayList<Intern> y2 = new ArrayList<Intern>();
//		ArrayList<Intern> y3 = new ArrayList<Intern>();
		ArrayList<Intern> y = new ArrayList<Intern>();
		for(Intern i:year1)
		{
			y.add(i);
		}
		for(Intern i:year2)
		{
			y.add(i);
		}
		for(Intern i:year3)
		{
			y.add(i);
		}
		Collections.shuffle(y);
		
		ArrayList<ArrayList<Intern>> teams = new ArrayList<ArrayList<Intern>>();
		while(y.size() > 3)
		{
			teams.add(makeWorkingTeam(y, 3));
			if(teams.contains(null))
			{
				return null;
			}
		}
		Intern[][] finalTeams = new Intern[teams.size()][teams.get(0).size()];
		for(int i=0; i<teams.size(); i++)
		{
			for(int j=0; j<teams.get(i).size(); j++)
			{
				finalTeams[i][j] = teams.get(i).get(j);
			}
		}
		

		return finalTeams;
	}
	
	public static ArrayList<Intern> makeWorkingTeam(ArrayList<Intern> interns, int size)
	{
		ArrayList<Intern> newTeam = new ArrayList<Intern>();
		
		while(newTeam.size() < size)
		{
			for(int i=0; i<interns.size(); i++)
			{
				boolean worksWithTeam = true;
				for(Intern in :newTeam)
				{
					worksWithTeam = worksWithTeam && in.worksWith(interns.get(i));
				}
				if(worksWithTeam)
				{
					newTeam.add(interns.get(i));
					interns.remove(i);
					break;
				}
				if(i==interns.size()-1)
				{
					return null;
				}
			}
		}
		
		return newTeam;
	}
	
	public static boolean teamsValid(Intern[][] teams)
	{
		for(int team=0; team<teams.length; team++)
		{
			int yearsum = 0;
			for(int ind=0; ind<teams[team].length; ind++)
			{
				yearsum += teams[team][ind].year;
				for(int oth=0; oth<teams[team].length; oth++)
				{
					if(!teams[team][oth].worksWith(teams[team][ind]))
					{
						return false;
					}
				}
			}
			if(yearsum == 3)
			{
				return false;
			}
		}
		return true;
	}
	
	public static class Intern implements Comparable<Intern>{
		String fName;
		String lName;
		String email;
		int year;
		Date start;
		String dayString;
		
		public Intern(String f, String l, String y, String email, String start)
		{
			fName = f;
			lName = l;
			year = Integer.parseInt(y);
			this.email=email;
			dayString = start.trim();
			//Yeah, there's better ways, but this is easy and works for this app
			String[] splitstart = start.split("/");
			int startDay = Integer.parseInt(splitstart[1]);
			int startMonth = Integer.parseInt(splitstart[0]);
			int startYear = Integer.parseInt(splitstart[2].strip());
			this.start = new Date(startYear, startMonth, startDay);
		}

		@Override
		public int compareTo(Intern o) {
			return this.start.compareTo(o.start);
		}

		private final long millisInTwoWeeks = 1210000000;
		public boolean worksWith(Intern o) {
//			System.out.println(this.start.getTime() - o.start.getTime());
			if(Math.abs(start.getTime()-o.start.getTime()) > millisInTwoWeeks)
			{
				return false;
			}
			if(year==3 && o.year==1)
			{
				return false;
			}
			if(year==1 && o.year==3)
			{
				return false;
			}
			return true;
		}
		
		public String getDayString() {
			return dayString;
		}
	}

}
