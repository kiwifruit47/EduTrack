import React from 'react';
import { Hourglass } from 'react-loader-spinner';

const Loader = () => {
    return (
        <div style={{ textAlign: "center", paddingTop: "20rem" }}>
          <Hourglass
            visible={true}
            height="80"
            width="80"
            ariaLabel="hourglass-loading"
            wrapperStyle={{}}
            wrapperClass=""
            colors={['#306cce', '#72a1ed']}
          />
        </div>
      );
}

export default Loader