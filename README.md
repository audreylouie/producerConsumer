# producerConsumer
Example of a producer-consumer problem implemented in a multithreaded application using mutex lock synchronization techniques.

# Synopsis
You are simulating a shift of a hamburger restaurant with two types of workers: cook and server. The restaurant can have multiple cooks and servers during the shift. A cook prepares for a tray of burgers (each tray can have one to five burgers). When a tray is ready, a cook will place the tray in one empty slot of the serving window. Each cook can make up to the pre-defined number of trays during their shift. A serving window contains a fixed number of slots available; if all slots are full, a cook needs to wait before placing their entry until a slot becomes available. A server picks up a tray from the serving window to serve it to the customers. During the simulation, you must ensure that all hamburgers cooked be served to the customers.
