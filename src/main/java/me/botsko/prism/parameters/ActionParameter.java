package me.botsko.prism.parameters;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionType;
import me.botsko.prism.actionlibs.MatchRule;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.appliers.PrismProcessType;
import me.botsko.prism.utils.LevenshteinDistance;

public class ActionParameter implements PrismParameterHandler {
	
	
	/**
	 * 
	 * @return
	 */
	public String getName(){
		return "Action";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String[] getHelp(){
		return new String[]{};
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Pattern getArgumentPattern(){
		return Pattern.compile("(a):([~|!]?[\\w,-]+)");
	}
	
	
	/**
	 * 
	 */
	public void process( QueryParameters query, Matcher input, CommandSender sender ){
		
		String[] actions = input.group(2).split(",");
		if(actions.length > 0){
			for(String action : actions){
				// Find all actions that match the action provided - whether the full name or
				// short name.
				ArrayList<ActionType> actionTypes = Prism.getActionRegistry().getActionsByShortname( action.replace("!", "") );
				if(!actionTypes.isEmpty()){
					for(ActionType actionType : actionTypes){
						
						// Ensure the action allows this process type
						if( (query.getProcessType().equals(PrismProcessType.ROLLBACK) && !actionType.canRollback()) || (query.getProcessType().equals(PrismProcessType.RESTORE) && !actionType.canRestore()) ){
							// @todo this is important information but is too spammy with a:place, because vehicle-place doesn't support a rollback etc
//							respond( sender, Prism.messenger.playerError("Ingoring action '"+actionType.getName()+"' because it doesn't support rollbacks.") );
							continue;
						}
						
						// Check match type
						MatchRule match = MatchRule.INCLUDE;
						if(action.startsWith("!")){
							match = MatchRule.EXCLUDE;
						}
						query.addActionType( actionType.getName(), match );
					}
				} else {
					if( sender != null ){
						sender.sendMessage( Prism.messenger.playerError("Ignoring action '"+action.replace("!", "")+"' because it's unrecognized. Did you mean '" + LevenshteinDistance.getClosestAction(action) +"'? Type '/prism params' for help.") );
					}
				}
			}
			// If none were valid, we end here.
			if(query.getActionTypes().size() == 0){
				throw new IllegalArgumentException("Action parameter value not recognized. Try /pr ? for help");
			}
		}
	}
	
	
	/**
	 * 
	 */
	public void defaultTo( QueryParameters query, CommandSender sender ){
		return;
	}
}