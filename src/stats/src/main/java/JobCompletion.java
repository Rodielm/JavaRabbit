class JobCompletion{
   private String worker;
   private String image;
   private long tsCreationMessage;
   private long tsReceptionWorker;
   private long tsFinalizationWorker;
   // Constructor
   // Setters & getters
public String getWorker() {
	return worker;
}
public void setWorker(String worker) {
	this.worker = worker;
}
public String getImage() {
	return image;
}
public void setImage(String image) {
	this.image = image;
}
public long getTsCreationMessage() {
	return tsCreationMessage;
}
public void setTsCreationMessage(long tsCreationMessage) {
	this.tsCreationMessage = tsCreationMessage;
}
public long getTsReceptionWorker() {
	return tsReceptionWorker;
}
public void setTsReceptionWorker(long tsReceptionWorker) {
	this.tsReceptionWorker = tsReceptionWorker;
}
public long getTsFinalizationWorker() {
	return tsFinalizationWorker;
}
public void setTsFinalizationWorker(long tsFinalizationWorker) {
	this.tsFinalizationWorker = tsFinalizationWorker;
}

   public JobCompletion() {
   }
   
}
