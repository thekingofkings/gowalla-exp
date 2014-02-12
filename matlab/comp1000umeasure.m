clear;
clc;

% load the variable com1000u, 
% which have the format
% user a ID, user b ID, weighted frequency, colocation entropy, our
% measure, frequency, friend flag
load comp1000umeasure.mat;


for condition = 0:5;
    

    [~, ind] = sort(com1000u(:,4));
    com1000u = com1000u(ind, :);

    com1000u = com1000u(com1000u(:,4)> condition,:);
   

    weightedFreq = com1000u(:,3);
    colocationEntro = com1000u(:,4);
    ourMeasure = com1000u(:,5);
    freq = com1000u(:,6);
    friLabel = com1000u(:,7);


    figure();
    % prec_rec( dm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
    % prec_rec( df5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
    prec_rec( weightedFreq, friLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
hold on;
    prec_rec( colocationEntro, friLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
    
    prec_rec( ourMeasure, friLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:' );

    prec_rec( freq, friLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--' );


    title(num2str(condition));
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'weighted frequency', 'colocation entropy', '1 - exp measure', 'freq'}, 'location', 'best');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    % print(['prl-50m', num2str(c), 'c1000u.eps'], '-dpsc');
    % system(['epstopdf prl-50m', num2str(c), 'c1000u.eps']);
    saveas(gcf, ['freq-wfbu1000fgt',num2str(condition),'-atan.png']);
    saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'-atan.fig']);
end