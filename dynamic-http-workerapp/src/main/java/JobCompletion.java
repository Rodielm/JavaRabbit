class JobCompletion{
   private String worker;
   private String image;
   private long tsCreationMessage;
   private long tsReceptionWorker;
   private long tsFinalizationWorker;

    public JobCompletion() {
    }
   

    public String getWorker() {
        return this.worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getTsCreationMessage() {
        return this.tsCreationMessage;
    }

    public void setTsCreationMessage(long tsCreationMessage) {
        this.tsCreationMessage = tsCreationMessage;
    }

    public long getTsReceptionWorker() {
        return this.tsReceptionWorker;
    }

    public void setTsReceptionWorker(long tsReceptionWorker) {
        this.tsReceptionWorker = tsReceptionWorker;
    }

    public long getTsFinalizationWorker() {
        return this.tsFinalizationWorker;
    }

    public void setTsFinalizationWorker(long tsFinalizationWorker) {
        this.tsFinalizationWorker = tsFinalizationWorker;
    }
}
