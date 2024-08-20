import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {

    private final int requestLimit;
    private final long timeWindowMillis;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private long lastResetTime = System.currentTimeMillis();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeWindowMillis = timeUnit.toMillis(1);
    }

    public void createDocument(Document document, String signature) throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastResetTime > timeWindowMillis) {
                    lastResetTime = currentTime;
                    requestCount.set(0);
                }

                if (requestCount.get() < requestLimit) {
                    requestCount.incrementAndGet();
                    break;
                } else {
                    long waitTime = timeWindowMillis - (currentTime - lastResetTime);
                    if (waitTime > 0) {
                        condition.await(waitTime, TimeUnit.MILLISECONDS);
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        simulateApiCall(document, signature);
    }

    private void simulateApiCall(Document document, String signature) {
        System.out.println("API call simulated:");
        System.out.println("Document ID: " + document.docId);
        System.out.println("Signature: " + signature);
    }

    public static class Document {
        public String docId;
        public String docStatus;
        public String docType;
        public boolean importRequest;
        public String ownerInn;
        public String participantInn;
        public String producerInn;
        public String productionDate;
        public String productionType;
        public Product[] products;
        public String regDate;
        public String regNumber;

        public static class Product {
            public String certificateDocument;
            public String certificateDocumentDate;
            public String certificateDocumentNumber;
            public String ownerInn;
            public String producerInn;
            public String productionDate;
            public String tnvedCode;
            public String uitCode;
            public String uituCode;
        }
    }
}
