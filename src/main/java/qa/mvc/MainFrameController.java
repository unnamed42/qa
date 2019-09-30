package qa.mvc;

import qa.api.QAApi;

class MainFrameController implements AutoCloseable {

    private final QAApi api = new QAApi();

    public void close() {
        api.close();
    }


}
