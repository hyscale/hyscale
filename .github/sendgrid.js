#! /usr/bin/env node

const sgMail = require('@sendgrid/mail');
sgMail.setApiKey(process.env.SENDGRID_API_KEY);

const fs = require('fs'),
    filename = 'hyscale_version.txt',
    fileType = 'application/txt',
    data = fs.readFileSync(filename);

const msg = {
    to: 'engineering@hyscale.io',
    from: 'niranjan.andhe@hyscale.io',
    subject: 'Hyscale Tool Build Status',
    text: 'Find the Attached Doc',
    html: '<p>Find the Attached DOC</p>',
    attachments: [
        {
            content: data.toString('base64'),
            filename: filename,
            type: fileType,
            disposition: 'attachment',
        },
    ],
};

sgMail
    .send(msg)
    .then(() => console.log('Mail sent successfully'))
    .catch(error => console.error(error.toString()));
