import React from "react";
import SwaggerUI from "swagger-ui-react";
import 'swagger-ui-react/swagger-ui.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Form } from 'react-bootstrap';
const SwaggerUIComponent = () => {
    const urls = [
        {url: 'https://dev.retrades.trade/api/main/v1/api-docs', name:  'Main Service'},
        {url: 'https://dev.retrades.trade/api/feedback-notification/v1/api-docs', name: 'Feedback Notification'},
        {url: 'https://dev.retrades.trade/api/storage/v1/api-docs', name: 'Storage Service'},
        {url: 'https://dev.retrades.trade/api/voucher/v1/api-docs', name: 'Voucher Service'}
    ]
    const [selectedUrl, setSelectedUrl] = React.useState(urls[0].url);

    return (
        <Container>
        <h1 className="my-4">Select an API Documentation</h1>
        <Form.Group controlId="apiSelect">
          <Form.Control 
            as="select" 
            onChange={(e) => setSelectedUrl(e.target.value)} 
            value={selectedUrl}
          >
            {urls.map((api) => (
              <option key={api.name} value={api.url}>
                {api.name}
              </option>
            ))}
          </Form.Control>
        </Form.Group>
  
        <SwaggerUI url={selectedUrl} />
      </Container>
    )
}

export default SwaggerUIComponent;