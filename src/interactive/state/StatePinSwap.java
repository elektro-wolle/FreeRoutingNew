/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * PinSwapState.java
 *
 * Created on 28. Maerz 2005, 09:25
 */

package interactive.state;

import freert.planar.PlaPointFloat;
import interactive.Actlog;
import interactive.IteraBoard;
import board.items.BrdAbitPin;
import board.items.BrdItem;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 *
 * @author Alfons Wirtz
 */
public class StatePinSwap extends StateInteractive
   {
   private final BrdAbitPin from_pin;
   private BrdAbitPin to_pin = null;
   private java.util.Set<BrdAbitPin> swappable_pins;
   
   public static StateInteractive get_instance(BrdAbitPin p_pin_to_swap, StateInteractive p_return_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      StatePinSwap new_state = new StatePinSwap(p_pin_to_swap, p_return_state, p_board_handling, p_logfile);
      if (new_state.swappable_pins.isEmpty())
         {
         new_state.i_brd.screen_messages.set_status_message(new_state.resources.getString("no_swappable_pin_found"));
         return p_return_state;
         }
      new_state.i_brd.screen_messages.set_status_message(new_state.resources.getString("please_click_second_pin_with_the_left_mouse_button"));
      return new_state;
      }

   /** Creates a new instance of PinSwapState */
   private StatePinSwap(BrdAbitPin p_pin_to_swap, StateInteractive p_return_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_return_state, p_board_handling, p_logfile);
      from_pin = p_pin_to_swap;
      swappable_pins = p_pin_to_swap.get_swappable_pins();
      }

   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      ItemSelectionFilter selection_filter = new ItemSelectionFilter(ItemSelectionChoice.PINS);
      java.util.Collection<BrdItem> picked_items = i_brd.pick_items(p_location, selection_filter);
      if (picked_items.isEmpty())
         {
         i_brd.screen_messages.set_status_message(resources.getString("no_pin_selected"));
         return cancel();
         }
      BrdItem to_item = picked_items.iterator().next();
      if (!(to_item instanceof BrdAbitPin))
         {
         i_brd.screen_messages.set_status_message(resources.getString("picked_pin_expected"));
         return cancel();
         }

      to_pin = (BrdAbitPin) to_item;
      if (!swappable_pins.contains(to_pin))
         {
         return cancel();
         }
      return complete();
      }

   @Override
   public StateInteractive complete()
      {
      if (from_pin == null || to_pin == null)
         {
         i_brd.screen_messages.set_status_message(resources.getString("pin_to_swap_missing"));
         return cancel();
         }
      if (from_pin.net_count() > 1 || to_pin.net_count() > 1)
         {
         System.out.println("PinSwapState.complete: pin swap not yet implemented for pins belonging to more than 1 net ");
         return cancel();
         }
      int from_net_no;
      if (from_pin.net_count() > 0)
         {
         from_net_no = from_pin.get_net_no(0);
         }
      else
         {
         from_net_no = -1;
         }
      int to_net_no;
      if (to_pin.net_count() > 0)
         {
         to_net_no = to_pin.get_net_no(0);
         }
      else
         {
         to_net_no = -1;
         }
      
      if (!r_brd.check_change_net(from_pin, to_net_no))
         {
         i_brd.screen_messages.set_status_message(resources.getString("pin_not_swapped_because_it_is_already_connected"));
         return cancel();
         }
      
      if (!r_brd.check_change_net(to_pin, from_net_no))
         {
         i_brd.screen_messages.set_status_message(resources.getString("pin_not_swapped_because_second_pin_is_already_connected"));
         return cancel();
         }
      
      r_brd.generate_snapshot();
      from_pin.swap(to_pin);
      for (int i = 0; i < from_pin.net_count(); ++i)
         {
         i_brd.update_ratsnest(from_pin.get_net_no(i));
         }
      for (int i = 0; i < to_pin.net_count(); ++i)
         {
         i_brd.update_ratsnest(to_pin.get_net_no(i));
         }
      i_brd.screen_messages.set_status_message(resources.getString("pin_swap_completed"));
      return return_state;
      }

   public void draw(java.awt.Graphics p_graphics)
      {
      java.awt.Color highlight_color = i_brd.gdi_context.get_hilight_color();
      double highligt_color_intensity = i_brd.gdi_context.get_hilight_color_intensity();
      from_pin.draw(p_graphics, i_brd.gdi_context, highlight_color, 0.5 * highligt_color_intensity);
      for (BrdAbitPin curr_pin : swappable_pins)
         {
         curr_pin.draw(p_graphics, i_brd.gdi_context, highlight_color, highligt_color_intensity);
         }
      }
   }
