#include <stdio.h>
#include <stdlib.h>

/*
	PA2 - A
	Alternating Bit Protocol
	CS 4480
	
	Student section completed by: 
	Melynda Lindhorst, u0855554
*/

/* ******************************************************************
 ALTERNATING BIT AND GO-BACK-N NETWORK EMULATOR: VERSION 1.1  J.F.Kurose

     Network properties:
   - one way network delay averages five time units (longer if there
     are other messages in the channel for GBN), but can be larger
   - packets can be corrupted (either the header or the data portion)
     or lost, according to user-defined probabilities
   - packets will be delivered in the order in which they were sent
     (although some can be lost).
**********************************************************************/

#define BIDIRECTIONAL 0    /* change to 1 if you're doing extra credit */
                           /* and write a routine called B_output */

/* a "msg" is the data unit passed from layer 5 (teachers code) to layer  */
/* 4 (students' code).  It contains the data (characters) to be delivered */
/* to layer 5 via the students transport level protocol entities.         */
struct msg {
  char data[20];
  };

/* a packet is the data unit passed from layer 4 (students code) to layer */
/* 3 (teachers code).  Note the pre-defined packet structure, which all   */
/* students must follow. */
struct pkt {
   int seqnum;
   int acknum;
   int checksum;
   char payload[20];
    };

/********* STUDENTS WRITE THE NEXT SEVEN ROUTINES *********/

/* global state variables */
struct pkt window[8];
struct msg buffy[50];
struct pkt current_packet;
struct pkt current_ACK;
int base;
int next_seq;
int buffy_read;
int buffy_write;
int buffy_count;
int expected_seq;
int total_buffered;
int total_dropped;
int total_interrupts;
int total_received;
int total_corrupted;
int total_packets;

/* generate statistics */
void print_statistics()
{
	printf("\n\n--- Session Statistics ---\n");
	printf("Total packets sent: %d\n", total_packets);
	printf("Total packets received: %d\n", total_received);
	printf("Total corrupted packets: %d\n", total_corrupted);
	printf("Total packets lost: %d\n", total_packets - total_received - 1); // Accounting for last packet never finishing its cycle before the simulator finishes.
	printf("Total packets dropped from layer 5: %d\n", total_dropped);
	printf("Total packets buffered: %d\n", total_buffered);
	printf("Total interrupts: %d\n", total_interrupts);
}

/* generate checksum for packets */
int generate_checksum(packet)
	struct pkt packet;
{
	int i;
	int checksum = 0;
	
	checksum += packet.seqnum;
	checksum += packet.acknum;
	
	for(i = 0; i < 20; i++)
	{
		checksum += packet.payload[i];
	}
	
	return checksum;
}

/* called from layer 5, passed the data to be sent to other side */
A_output(message)
  struct msg message;
{
	int i;
	struct pkt to_send;
	struct msg send_msg;
	
	printf("\n\n- A Output -\n");
	
	// If there is availability in the window, else buffer, else drop.
	if(next_seq < base + 7)
	{
		total_packets ++;
		
		send_msg = buffy[buffy_read];
		
		// React buffer.
		buffy_count --;
		if(buffy_read == 50)
		{
			buffy_read = 0;
		}
		else
		{
			buffy_read ++;
		}
	
		// Setup to_send packet.
		to_send.seqnum = next_seq;
		to_send.acknum = 0;
		
		for(i = 0; i < 20; i++)
		{
			to_send.payload[i] = send_msg.data[i];
			current_packet.payload[i] = send_msg.data[i];
		}
		
		to_send.checksum = generate_checksum(to_send);
		
		// Set this packet to current_packet.
		current_packet.seqnum = to_send.seqnum;
		current_packet.checksum = to_send.checksum;
		
		// Add packet to window.
		window[to_send.seqnum % 8] = to_send;
		
		// Start timer if window is new.
		if(base == next_seq)
		{
			starttimer(0, 15.0); 
		}
		
		printf("Sending packet. \n");
		next_seq ++;
		
		// Begin sending over layer 3.
		tolayer3(0, to_send);
	}
	else if(buffy_count < 50)
	{
		buffy[buffy_write] = message;
		
		total_buffered ++;
		
		// React buffer.
		buffy_count ++;
		if(buffy_write == 50)
		{
			buffy_write = 0;
		}
		else
		{
			buffy_write ++;
		}
		
		printf("Buffered packet. \n");
	}
	else
	{
		printf("Buffer full. Exiting program.\n");
		exit(0);
	}
}

B_output(message)  /* need be completed only for extra credit */
  struct msg message;
{
	
}

/* called from layer 3, when a packet arrives for layer 4 */
A_input(packet)
  struct pkt packet;
{
	int new_checksum = generate_checksum(packet);
	total_received ++;
	printf("\n\n- A Input - \n");
	
	// Check for corruption or wrong acknum
	if(current_ACK.checksum == new_checksum && current_ACK.acknum == packet.acknum && current_ACK.seqnum == packet.seqnum && current_packet.seqnum == packet.acknum)
	{
		base = packet.acknum + 1;
		
		if(base == next_seq)
		{
			stoptimer(0);
		}
		else
		{
			starttimer(0, 15.0);
		}
		
		printf("ACK accepted. %d\n", base);
		//printf("Received checksum: %d\n", new_checksum);
		//printf("Received %s.\n", packet.acknum == 0 ? "ACK0" : "ACK1");
	}
	else
	{
		// Only increcment total_corrupted if the ACK is corrupted, not just an unexpected ACK.
		if(current_ACK.checksum != new_checksum || current_ACK.acknum != packet.acknum || current_ACK.seqnum != packet.seqnum)
		{
			total_corrupted ++;
			printf("Packet from B was corrupted. Wait for timeout.\n");
		}
		else
		{
			printf("Unexpected ACK from B. Wait for timeout.\n");
		}
		
		//printf("Received checksum: %d\n", new_checksum);
		//printf("Received %s.\n", packet.acknum == 0 ? "ACK0" : "ACK1");
	}
}

/* called when A's timer goes off */
A_timerinterrupt()
{
	struct msg resend_message;
	int i;
	total_interrupts ++;
	
	printf("\n\n - A Timer Interrupt - \n");
	printf("Timeout occured in A. Resending window.\n");
	
	starttimer(0, 15.0);
	
	for(i = 0; i < 8; i++)
	{
		total_packets ++;
		tolayer3(0, window[i]);
	}
}  

/* the following routine will be called once (only) before any other */
/* entity A routines are called. You can use it to do any initialization */
A_init()
{
	int i;
	
	// Initialize current_packet
	current_packet.seqnum = 0;
	current_packet.acknum = 0;
	current_packet.checksum = 0;
	
	for(i = 0; i < 20; i++)
	{
		current_packet.payload[i] = 0;
	}
	
	// Initialize other variables
	base = 0;
	next_seq = 0;
	buffy_read = 0;
	buffy_write = 0;
	buffy_count = 0;
	total_buffered = 0;
	total_dropped = 0;
	total_interrupts = 0;
	total_received = 0;
	total_corrupted = 0;
	total_packets = 0;
}


/* Note that with simplex transfer from a-to-B, there is no B_output() */

/* called from layer 3, when a packet arrives for layer 4 at B*/
B_input(packet)
  struct pkt packet;
{
	int i;
	struct msg to_up;
	struct pkt ack;
	total_packets ++;
	total_received ++;
	int new_checksum = generate_checksum(packet);
	
	printf("\n\n - B Input- \n");
	
	// Check for corruption and seqnum
	if(current_packet.checksum == new_checksum && current_packet.seqnum == packet.seqnum && current_packet.acknum == packet.acknum  && expected_seq == packet.seqnum)
	{
		for(i = 0; i < 20; i++)
		{
			to_up.data[i] = packet.payload[i];
			ack.payload[i] = packet.payload[i];
			current_ACK.payload[i] = packet.payload[i];
		}
		
		tolayer5(1, to_up);
		
		ack.acknum = packet.seqnum;
		ack.seqnum = packet.seqnum;
		current_ACK.acknum = packet.seqnum;
		current_ACK.seqnum = packet.seqnum;
		ack.checksum = generate_checksum(ack);
		current_ACK.checksum = ack.checksum;
		
		expected_seq ++;
		
		printf("Packet intact. Sending ACK.\n");
		//printf("Sending checksum: %d\n", ack.checksum);
		//printf("Sending %s.\n", ack.acknum == 0 ? "ACK0" : "ACK1");
		
		tolayer3(1, ack);
	}
	else
	{
		total_corrupted ++;
		
		ack.acknum = base;
		ack.seqnum = base;
		current_ACK.acknum = ack.acknum;
		current_ACK.seqnum = ack.acknum;
		ack.checksum = generate_checksum(ack);
		current_ACK.checksum = ack.checksum;
		
		printf("Packet from A was corrupted. Sending expected ACK. \n");
		
		tolayer3(1, ack);
	}
	
}

/* called when B's timer goes off */
B_timerinterrupt()
{
}

/* the following rouytine will be called once (only) before any other */
/* entity B routines are called. You can use it to do any initialization */
B_init()
{
	int i; 
	
	expected_seq = 0;
	
	current_ACK.seqnum = 0;
	current_ACK.acknum = 0;
	current_ACK.checksum = 0;
	
	for(i = 0; i < 20; i++)
	{
		current_ACK.payload[i] = 0;
	}
}


/*****************************************************************
***************** NETWORK EMULATION CODE STARTS BELOW ***********
The code below emulates the layer 3 and below network environment:
  - emulates the tranmission and delivery (possibly with bit-level corruption
    and packet loss) of packets across the layer 3/4 interface
  - handles the starting/stopping of a timer, and generates timer
    interrupts (resulting in calling students timer handler).
  - generates message to be sent (passed from later 5 to 4)

THERE IS NOT REASON THAT ANY STUDENT SHOULD HAVE TO READ OR UNDERSTAND
THE CODE BELOW.  YOU SHOLD NOT TOUCH, OR REFERENCE (in your code) ANY
OF THE DATA STRUCTURES BELOW.  If you're interested in how I designed
the emulator, you're welcome to look at the code - but again, you should have
to, and you defeinitely should not have to modify
******************************************************************/

struct event {
   float evtime;           /* event time */
   int evtype;             /* event type code */
   int eventity;           /* entity where event occurs */
   struct pkt *pktptr;     /* ptr to packet (if any) assoc w/ this event */
   struct event *prev;
   struct event *next;
 };
struct event *evlist = NULL;   /* the event list */

/* possible events: */
#define  TIMER_INTERRUPT 0  
#define  FROM_LAYER5     1
#define  FROM_LAYER3     2

#define  OFF             0
#define  ON              1
#define   A    0
#define   B    1



int TRACE = 1;             /* for my debugging */
int nsim = 0;              /* number of messages from 5 to 4 so far */ 
int nsimmax = 0;           /* number of msgs to generate, then stop */
float time = 0.000;
float lossprob;            /* probability that a packet is dropped  */
float corruptprob;         /* probability that one bit is packet is flipped */
float lambda;              /* arrival rate of messages from layer 5 */   
int   ntolayer3;           /* number sent into layer 3 */
int   nlost;               /* number lost in media */
int ncorrupt;              /* number corrupted by media*/

main()
{
   struct event *eventptr;
   struct msg  msg2give;
   struct pkt  pkt2give;
   
   int i,j;
   char c; 
  
   init();
   A_init();
   B_init();
   
   while (1) {
        eventptr = evlist;            /* get next event to simulate */
        if (eventptr==NULL)
           goto terminate;
        evlist = evlist->next;        /* remove this event from event list */
        if (evlist!=NULL)
           evlist->prev=NULL;
        if (TRACE>=2) {
           printf("\nEVENT time: %f,",eventptr->evtime);
           printf("  type: %d",eventptr->evtype);
           if (eventptr->evtype==0)
	       printf(", timerinterrupt  ");
             else if (eventptr->evtype==1)
               printf(", fromlayer5 ");
             else
	     printf(", fromlayer3 ");
           printf(" entity: %d\n",eventptr->eventity);
           }
        time = eventptr->evtime;        /* update time to next event time */
        if (nsim==nsimmax)
	  break;                        /* all done with simulation */
        if (eventptr->evtype == FROM_LAYER5 ) {
            generate_next_arrival();   /* set up future arrival */
            /* fill in msg to give with string of same letter */    
            j = nsim % 26; 
            for (i=0; i<20; i++)  
               msg2give.data[i] = 97 + j;
            if (TRACE>2) {
               printf("          MAINLOOP: data given to student: ");
                 for (i=0; i<20; i++) 
                  printf("%c", msg2give.data[i]);
               printf("\n");
	     }
            nsim++;
            if (eventptr->eventity == A) 
               A_output(msg2give);  
             else
               B_output(msg2give);  
            }
          else if (eventptr->evtype ==  FROM_LAYER3) {
            pkt2give.seqnum = eventptr->pktptr->seqnum;
            pkt2give.acknum = eventptr->pktptr->acknum;
            pkt2give.checksum = eventptr->pktptr->checksum;
            for (i=0; i<20; i++)  
                pkt2give.payload[i] = eventptr->pktptr->payload[i];
	    if (eventptr->eventity ==A)      /* deliver packet by calling */
   	       A_input(pkt2give);            /* appropriate entity */
            else
   	       B_input(pkt2give);
	    free(eventptr->pktptr);          /* free the memory for packet */
            }
          else if (eventptr->evtype ==  TIMER_INTERRUPT) {
            if (eventptr->eventity == A) 
	       A_timerinterrupt();
             else
	       B_timerinterrupt();
             }
          else  {
	     printf("INTERNAL PANIC: unknown event type \n");
             }
        free(eventptr);
        }

terminate:
   printf(" Simulator terminated at time %f\n after sending %d msgs from layer5\n",time,nsim);
   
   print_statistics();
}



init()                         /* initialize the simulator */
{
  int i;
  float sum, avg;
  float jimsrand();
  
  
   printf("-----  Stop and Wait Network Simulator Version 1.1 -------- \n\n");
   printf("Enter the number of messages to simulate: ");
   scanf("%d",&nsimmax);
   printf("Enter  packet loss probability [enter 0.0 for no loss]:");
   scanf("%f",&lossprob);
   printf("Enter packet corruption probability [0.0 for no corruption]:");
   scanf("%f",&corruptprob);
   printf("Enter average time between messages from sender's layer5 [ > 0.0]:");
   scanf("%f",&lambda);
   printf("Enter TRACE:");
   scanf("%d",&TRACE);

   srand(9999);              /* init random number generator */
   sum = 0.0;                /* test random number generator for students */
   for (i=0; i<1000; i++)
      sum=sum+jimsrand();    /* jimsrand() should be uniform in [0,1] */
   avg = sum/1000.0;
   if (avg < 0.25 || avg > 0.75) {
    printf("It is likely that random number generation on your machine\n" ); 
    printf("is different from what this emulator expects.  Please take\n");
    printf("a look at the routine jimsrand() in the emulator code. Sorry. \n");
    exit(-1);
    }

   ntolayer3 = 0;
   nlost = 0;
   ncorrupt = 0;

   time=0.0;                    /* initialize time to 0.0 */
   generate_next_arrival();     /* initialize event list */
}

/****************************************************************************/
/* jimsrand(): return a float in range [0,1].  The routine below is used to */
/* isolate all random number generation in one location.  We assume that the*/
/* system-supplied rand() function return an int in therange [0,mmm]        */
/****************************************************************************/
float jimsrand() 
{
  double mmm = RAND_MAX;   /* largest int  - MACHINE DEPENDENT!!!!!!!!   */
  float x;                   /* individual students may need to change mmm */ 
  x = rand()/mmm;            /* x should be uniform in [0,1] */
  return(x);
}  

/********************* EVENT HANDLINE ROUTINES *******/
/*  The next set of routines handle the event list   */
/*****************************************************/
 
generate_next_arrival()
{
   double x,log(),ceil();
   struct event *evptr;
   //    char *malloc();
   float ttime;
   int tempint;

   if (TRACE>2)
       printf("          GENERATE NEXT ARRIVAL: creating new arrival\n");
 
   x = lambda*jimsrand()*2;  /* x is uniform on [0,2*lambda] */
                             /* having mean of lambda        */
   evptr = (struct event *)malloc(sizeof(struct event));
   evptr->evtime =  time + x;
   evptr->evtype =  FROM_LAYER5;
   if (BIDIRECTIONAL && (jimsrand()>0.5) )
      evptr->eventity = B;
    else
      evptr->eventity = A;
   insertevent(evptr);
} 


insertevent(p)
   struct event *p;
{
   struct event *q,*qold;

   if (TRACE>2) {
      printf("            INSERTEVENT: time is %lf\n",time);
      printf("            INSERTEVENT: future time will be %lf\n",p->evtime); 
      }
   q = evlist;     /* q points to header of list in which p struct inserted */
   if (q==NULL) {   /* list is empty */
        evlist=p;
        p->next=NULL;
        p->prev=NULL;
        }
     else {
        for (qold = q; q !=NULL && p->evtime > q->evtime; q=q->next)
              qold=q; 
        if (q==NULL) {   /* end of list */
             qold->next = p;
             p->prev = qold;
             p->next = NULL;
             }
           else if (q==evlist) { /* front of list */
             p->next=evlist;
             p->prev=NULL;
             p->next->prev=p;
             evlist = p;
             }
           else {     /* middle of list */
             p->next=q;
             p->prev=q->prev;
             q->prev->next=p;
             q->prev=p;
             }
         }
}

printevlist()
{
  struct event *q;
  int i;
  printf("--------------\nEvent List Follows:\n");
  for(q = evlist; q!=NULL; q=q->next) {
    printf("Event time: %f, type: %d entity: %d\n",q->evtime,q->evtype,q->eventity);
    }
  printf("--------------\n");
}



/********************** Student-callable ROUTINES ***********************/

/* called by students routine to cancel a previously-started timer */
stoptimer(AorB)
int AorB;  /* A or B is trying to stop timer */
{
 struct event *q,*qold;

 if (TRACE>2)
    printf("          STOP TIMER: stopping timer at %f\n",time);
/* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
 for (q=evlist; q!=NULL ; q = q->next) 
    if ( (q->evtype==TIMER_INTERRUPT  && q->eventity==AorB) ) { 
       /* remove this event */
       if (q->next==NULL && q->prev==NULL)
             evlist=NULL;         /* remove first and only event on list */
          else if (q->next==NULL) /* end of list - there is one in front */
             q->prev->next = NULL;
          else if (q==evlist) { /* front of list - there must be event after */
             q->next->prev=NULL;
             evlist = q->next;
             }
           else {     /* middle of list */
             q->next->prev = q->prev;
             q->prev->next =  q->next;
             }
       free(q);
       return;
     }
  printf("Warning: unable to cancel your timer. It wasn't running.\n");
}


starttimer(AorB,increment)
int AorB;  /* A or B is trying to stop timer */
float increment;
{

 struct event *q;
 struct event *evptr;
 //char *malloc();

 if (TRACE>2)
    printf("          START TIMER: starting timer at %f\n",time);
 /* be nice: check to see if timer is already started, if so, then  warn */
/* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
   for (q=evlist; q!=NULL ; q = q->next)  
    if ( (q->evtype==TIMER_INTERRUPT  && q->eventity==AorB) ) { 
      printf("Warning: attempt to start a timer that is already started\n");
      return;
      }
 
/* create future event for when timer goes off */
   evptr = (struct event *)malloc(sizeof(struct event));
   evptr->evtime =  time + increment;
   evptr->evtype =  TIMER_INTERRUPT;
   evptr->eventity = AorB;
   insertevent(evptr);
} 


/************************** TOLAYER3 ***************/
tolayer3(AorB,packet)
int AorB;  /* A or B is trying to stop timer */
struct pkt packet;
{
 struct pkt *mypktptr;
 struct event *evptr,*q;
 //char *malloc();
 float lastime, x, jimsrand();
 int i;


 ntolayer3++;

 /* simulate losses: */
 if (jimsrand() < lossprob)  {
      nlost++;
      if (TRACE>0)    
	printf("          TOLAYER3: packet being lost\n");
      return;
    }  

/* make a copy of the packet student just gave me since he/she may decide */
/* to do something with the packet after we return back to him/her */ 
 mypktptr = (struct pkt *)malloc(sizeof(struct pkt));
 mypktptr->seqnum = packet.seqnum;
 mypktptr->acknum = packet.acknum;
 mypktptr->checksum = packet.checksum;
 for (i=0; i<20; i++)
    mypktptr->payload[i] = packet.payload[i];
 if (TRACE>2)  {
   printf("          TOLAYER3: seq: %d, ack %d, check: %d ", mypktptr->seqnum,
	  mypktptr->acknum,  mypktptr->checksum);
    for (i=0; i<20; i++)
        printf("%c",mypktptr->payload[i]);
    printf("\n");
   }

/* create future event for arrival of packet at the other side */
  evptr = (struct event *)malloc(sizeof(struct event));
  evptr->evtype =  FROM_LAYER3;   /* packet will pop out from layer3 */
  evptr->eventity = (AorB+1) % 2; /* event occurs at other entity */
  evptr->pktptr = mypktptr;       /* save ptr to my copy of packet */
/* finally, compute the arrival time of packet at the other end.
   medium can not reorder, so make sure packet arrives between 1 and 10
   time units after the latest arrival time of packets
   currently in the medium on their way to the destination */
 lastime = time;
/* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next) */
 for (q=evlist; q!=NULL ; q = q->next) 
    if ( (q->evtype==FROM_LAYER3  && q->eventity==evptr->eventity) ) 
      lastime = q->evtime;
 evptr->evtime =  lastime + 1 + 9*jimsrand();
 


 /* simulate corruption: */
 if (jimsrand() < corruptprob)  {
    ncorrupt++;
    if ( (x = jimsrand()) < .75)
       mypktptr->payload[0]='Z';   /* corrupt payload */
      else if (x < .875)
       mypktptr->seqnum = 999999;
      else
       mypktptr->acknum = 999999;
    if (TRACE>0)    
	printf("          TOLAYER3: packet being corrupted\n");
    }  

  if (TRACE>2)  
     printf("          TOLAYER3: scheduling arrival on other side\n");
  insertevent(evptr);
} 

tolayer5(AorB,datasent)
  int AorB;
  char datasent[20];
{
  int i;  
  if (TRACE>2) {
     printf("          TOLAYER5: data received: ");
     for (i=0; i<20; i++)  
        printf("%c",datasent[i]);
     printf("\n");
   }
  
}