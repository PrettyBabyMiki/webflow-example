package org.springframework.webflow.samples.booking.flow.main;

import java.util.List;

import org.springframework.validation.DataBinder;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.samples.booking.app.Booking;
import org.springframework.webflow.samples.booking.app.BookingService;
import org.springframework.webflow.samples.booking.app.Hotel;
import org.springframework.webflow.samples.booking.app.User;

/**
 * Actions invoked by the main flow. These actions are extensions of the flow definition, called by the flow definition
 * at the appropriate points. Actions allow an externalized flow definition to delegate out to Java code to perform
 * processing.
 */
public class MainActions extends FormAction {

    private BookingService bookingService;

    /**
     * Constructs a new multi-action for the main flow that will delegate to the provided booking service
     * @param bookingService the booking service
     */
    public MainActions(BookingService bookingService) {
	this.bookingService = bookingService;
    }

    /**
     * Simply put a dummy user in conversation scope to simulate a user login. In the future this sample may add user
     * authentication support.
     * @param context the current flow execution request context
     * @return success
     */
    public Event initCurrentUser(RequestContext context) {
	User user = new User("springer", "springrocks", "Springer");
	context.getConversationScope().put("user", user);
	return success();
    }

    /**
     * Find all active bookings made by the current user.
     * @param context the current flow execution request context
     * @return success
     */
    public Event findCurrentUserBookings(RequestContext context) {
	User user = (User) context.getConversationScope().get("user");
	List<Booking> bookings = bookingService.findBookings(user.getUsername());
	context.getFlowScope().put("bookings", bookings);
	return success();
    }

    /**
     * Find all hotels that meet the current search criteria in flow scope.
     * @param context the current flow execution request context
     * @return success
     */
    public Event findHotels(RequestContext context) throws Exception {
	SearchCriteria search = (SearchCriteria) context.getFlowScope().get("searchCriteria");
	DataBinder binder = createBinder(context, search);
	doBind(context, binder);
	List<Hotel> hotels = bookingService
		.findHotels(search.getSearchString(), search.getPageSize(), search.getPage());
	context.getFlowScope().put("hotels", hotels);
	return success();
    }
}
